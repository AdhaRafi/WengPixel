package com.wengpixel.data.local.file

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.wengpixel.core.common.ImageUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageFileManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cacheImageDir: File
        get() = File(context.cacheDir, "images").apply { if (!exists()) mkdirs() }

    private val historyImageDir: File
        get() = File(context.filesDir, "history_images").apply { if (!exists()) mkdirs() }

    /**
     * Menyalin data dari Content Uri ke berkas privat sementara di cache.
     */
    fun copyUriToTempCache(sourceUri: Uri, extension: String = "png"): File {
        val fileName = "temp_input_${UUID.randomUUID()}.$extension"
        val destFile = File(cacheImageDir, fileName)

        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }
        return destFile
    }

    /**
     * Menyimpan bitmap yang baru diproses ke berkas cache sementara.
     */
    fun saveBitmapToTempCache(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): File {
        val ext = if (format == Bitmap.CompressFormat.JPEG) "jpg" else "png"
        val fileName = "temp_edited_${UUID.randomUUID()}.$ext"
        val file = File(cacheImageDir, fileName)
        ImageUtils.saveBitmapToFile(bitmap, file, format, quality)
        return file
    }

    /**
     * Menyimpan byte array hasil response backend ke berkas cache sementara.
     */
    fun saveBytesToTempCache(bytes: ByteArray, extension: String = "png"): File {
        val fileName = "temp_remote_${UUID.randomUUID()}.$extension"
        val file = File(cacheImageDir, fileName)
        FileOutputStream(file).use { it.write(bytes) }
        return file
    }

    /**
     * Menyimpan hasil edit secara permanen ke direktori riwayat privat aplikasi.
     * Mengembalikan triple: (pathOriginal, pathEdited, pathThumbnail).
     */
    fun persistForHistory(
        originalFile: File,
        editedBitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG
    ): Triple<String, String, String> {
        val id = UUID.randomUUID().toString()
        val ext = if (format == Bitmap.CompressFormat.JPEG) "jpg" else "png"

        // 1. Simpan salinan original
        val origDest = File(historyImageDir, "orig_${id}.png")
        originalFile.copyTo(origDest, overwrite = true)

        // 2. Simpan hasil edit
        val editedDest = File(historyImageDir, "edit_${id}.$ext")
        ImageUtils.saveBitmapToFile(editedBitmap, editedDest, format, 100)

        // 3. Buat thumbnail ringan (maks 256px) untuk daftar riwayat
        val thumbDest = File(historyImageDir, "thumb_${id}.png")
        val maxThumbSize = 256
        val ratio = editedBitmap.width.toFloat() / editedBitmap.height.toFloat()
        val (thumbW, thumbH) = if (ratio > 1f) {
            maxThumbSize to (maxThumbSize / ratio).toInt().coerceAtLeast(1)
        } else {
            (maxThumbSize * ratio).toInt().coerceAtLeast(1) to maxThumbSize
        }
        val thumbBitmap = Bitmap.createScaledBitmap(editedBitmap, thumbW, thumbH, true)
        ImageUtils.saveBitmapToFile(thumbBitmap, thumbDest, Bitmap.CompressFormat.PNG, 90)
        if (thumbBitmap != editedBitmap) {
            thumbBitmap.recycle()
        }

        return Triple(origDest.absolutePath, editedDest.absolutePath, thumbDest.absolutePath)
    }

    /**
     * Mengekspor gambar ke galeri publik (Pictures/WengPixel) melalui MediaStore
     * dengan dukungan penuh Scoped Storage.
     */
    fun exportToGallery(
        bitmap: Bitmap,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): Uri? {
        val isPng = format == Bitmap.CompressFormat.PNG
        val mimeType = if (isPng) "image/png" else "image/jpeg"
        val ext = if (isPng) "png" else "jpg"
        val fileName = "WengPixel_${System.currentTimeMillis()}.$ext"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + "WengPixel"
                )
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val imageUri = resolver.insert(collection, contentValues) ?: return null

        try {
            resolver.openOutputStream(imageUri)?.use { out ->
                bitmap.compress(format, quality, out)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }
            return imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            // Bersihkan jika gagal
            resolver.delete(imageUri, null, null)
            return null
        }
    }

    /**
     * Membuat intent untuk membagikan berkas gambar melalui FileProvider.
     */
    fun createShareIntent(imageFile: File, mimeType: String = "image/png"): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Menghapus berkas fisik yang terkait dengan riwayat.
     */
    fun deleteHistoryFiles(originalPath: String, editedPath: String, thumbnailPath: String) {
        runCatching { File(originalPath).delete() }
        runCatching { File(editedPath).delete() }
        runCatching { File(thumbnailPath).delete() }
    }

    /**
     * Membersihkan berkas sementara di cache yang lebih tua dari batas usia tertentu.
     */
    fun cleanTempCache(maxAgeMillis: Long = 24 * 60 * 60 * 1000) {
        val now = System.currentTimeMillis()
        cacheImageDir.listFiles()?.forEach { file ->
            if (now - file.lastModified() > maxAgeMillis) {
                file.delete()
            }
        }
    }
}
