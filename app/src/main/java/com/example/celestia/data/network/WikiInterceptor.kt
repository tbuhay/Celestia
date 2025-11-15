package com.example.celestia.data.network

import okhttp3.Interceptor
import okhttp3.Response

class WikiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header(
                "User-Agent",
                "CelestiaApp/1.0 (https://yourdomain.com) Android-App"
            )
            .build()

        return chain.proceed(request)
    }
}