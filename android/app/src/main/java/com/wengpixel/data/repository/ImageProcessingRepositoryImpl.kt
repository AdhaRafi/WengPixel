package com.wengpixel.data.repository

import com.wengpixel.core.common.AppResult
import com.wengpixel.core.common.DispatcherProvider
import com.wengpixel.data.local.datastore.SettingsDataStore
import com.wengpixel.data.local.file.ImageFileManager
import com.wengpixel.data.remote.api.WengPixelApiService
import com.wengpixel.data.remote.dto.ErrorResponseDto
import com.wengpixel.data.remote.dto.ServerStatusDto
import com.wengpixel.domain.repository.ImageProcessingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageProcessingRepositoryImpl @Inject constructor(
    private val apiService: WengPixelApiService,
    private val fileManager: ImageFileManager,
    private val settingsDataStore: SettingsDataStore,
    private val dispatchers: DispatcherProvider
) : ImageProcessingRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun removeBackground(imageFile: File): Flow<AppResult<File>> = flow {
        emit(AppResult.Loading)
        try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            val response = apiService.removeBackground(body)
            if (response.isSuccessful && response.body() != null) {
                val bytes = response.body()!!.bytes()
                val resultFile = fileManager.saveBytesToTempCache(bytes, "png")
                emit(AppResult.Success(resultFile))
            } else {
                val errorMsg = parseErrorMessage(response.code(), response.errorBody()?.string())
                emit(AppResult.Error(Exception(errorMsg), errorMsg))
            }
        } catch (e: Exception) {
            val friendlyMsg = toFriendlyErrorMessage(e)
            emit(AppResult.Error(e, friendlyMsg))
        }
    }.flowOn(dispatchers.io)

    override fun upscaleImage(imageFile: File, scale: Int): Flow<AppResult<File>> = flow {
        emit(AppResult.Loading)
        try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
            val scaleBody = scale.toString().toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.upscale(body, scaleBody)
            if (response.isSuccessful && response.body() != null) {
                val bytes = response.body()!!.bytes()
                val resultFile = fileManager.saveBytesToTempCache(bytes, "png")
                emit(AppResult.Success(resultFile))
            } else {
                val errorMsg = parseErrorMessage(response.code(), response.errorBody()?.string())
                emit(AppResult.Error(Exception(errorMsg), errorMsg))
            }
        } catch (e: Exception) {
            val friendlyMsg = toFriendlyErrorMessage(e)
            emit(AppResult.Error(e, friendlyMsg))
        }
    }.flowOn(dispatchers.io)

    override suspend fun checkServerStatus(): AppResult<ServerStatusDto> {
        return try {
            val response = apiService.getStatus()
            if (response.isSuccessful && response.body() != null) {
                AppResult.Success(response.body()!!)
            } else {
                val msg = parseErrorMessage(response.code(), response.errorBody()?.string())
                AppResult.Error(Exception(msg), msg)
            }
        } catch (e: Exception) {
            val msg = toFriendlyErrorMessage(e)
            AppResult.Error(e, msg)
        }
    }

    private fun parseErrorMessage(code: Int, errorBody: String?): String {
        if (errorBody.isNullOrBlank()) {
            return when (code) {
                413 -> "Ukuran berkas terlalu besar untuk diproses."
                503 -> "Layanan pemrosesan AI belum siap atau belum dikonfigurasi di server."
                else -> "Gagal memproses gambar (Kode HTTP: $code)."
            }
        }

        try {
            val errorDto = json.decodeFromString<ErrorResponseDto>(errorBody)
            if (!errorDto.setupGuide.isNullOrBlank()) {
                return "${errorDto.message ?: "Layanan belum siap"}\nPanduan: ${errorDto.setupGuide}"
            }
            if (!errorDto.message.isNullOrBlank()) {
                return errorDto.message
            }
            if (!errorDto.detail.isNullOrBlank()) {
                return errorDto.detail
            }
        } catch (_: Exception) {
            // fallback text jika bukan JSON standar
        }

        return when (code) {
            503 -> "Penyedia AI belum diatur di backend. Periksa file .env pada server."
            413 -> "Ukuran gambar melebihi batas yang diizinkan."
            else -> "Terjadi kesalahan pada server ($code)."
        }
    }

    private suspend fun toFriendlyErrorMessage(e: Throwable): String {
        val currentUrl = settingsDataStore.backendUrl.first()
        return when (e) {
            is ConnectException, is UnknownHostException -> {
                "Tidak dapat terhubung ke server ($currentUrl). Pastikan backend aktif dan alamat server di menu Pengaturan sudah benar."
            }
            is SocketTimeoutException -> {
                "Waktu koneksi habis saat memproses gambar. Proses AI membutuhkan waktu lebih lama atau jaringan sedang lambat."
            }
            is IOException -> {
                "Terjadi kendala koneksi internet. Periksa koneksi data atau Wi-Fi Anda."
            }
            else -> e.localizedMessage ?: "Terjadi kesalahan tak terduga."
        }
    }
}
