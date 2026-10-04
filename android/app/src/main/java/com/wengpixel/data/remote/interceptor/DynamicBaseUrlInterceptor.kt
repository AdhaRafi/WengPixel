package com.wengpixel.data.remote.interceptor

import com.wengpixel.data.local.datastore.SettingsDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicBaseUrlInterceptor @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        val customUrl = runBlocking {
            settingsDataStore.backendUrl.first()
        }

        val newBaseHttpUrl = customUrl.toHttpUrlOrNull()
        if (newBaseHttpUrl != null) {
            val oldUrl = request.url
            val newUrl = oldUrl.newBuilder()
                .scheme(newBaseHttpUrl.scheme)
                .host(newBaseHttpUrl.host)
                .port(newBaseHttpUrl.port)
                .build()
            request = request.newBuilder().url(newUrl).build()
        }

        return chain.proceed(request)
    }
}
