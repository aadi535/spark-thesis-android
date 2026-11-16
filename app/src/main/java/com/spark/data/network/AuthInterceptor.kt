package com.spark.data.network

import com.spark.data.local.DataStoreManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val dataStoreManager: DataStoreManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { dataStoreManager.jwtTokenFlow.firstOrNull() }
        
        val requestBuilder = chain.request().newBuilder()
        if (chain.request().header("Authorization") == null && !token.isNullOrEmpty() && !chain.request().url.encodedPath.startsWith("/api/v1/auth/")) {
            requestBuilder.header("Authorization", "Bearer $token")
        }
        
        return chain.proceed(requestBuilder.build())
    }
}
