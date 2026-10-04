package com.wengpixel.data.remote.api

import com.wengpixel.data.remote.dto.ServerStatusDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface WengPixelApiService {

    @Multipart
    @POST("v1/process/remove-background")
    suspend fun removeBackground(
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @Multipart
    @POST("v1/process/upscale")
    suspend fun upscale(
        @Part file: MultipartBody.Part,
        @Part("scale") scale: RequestBody
    ): Response<ResponseBody>

    @GET("v1/status")
    suspend fun getStatus(): Response<ServerStatusDto>

    @GET("health")
    suspend fun checkHealth(): Response<Map<String, String>>
}
