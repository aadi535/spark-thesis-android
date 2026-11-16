package com.spark.di

import com.spark.BuildConfig
import com.spark.data.local.DataStoreManager
import com.spark.data.network.AuthInterceptor
import com.spark.data.network.SparkApiService
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.koin.androidx.viewmodel.dsl.viewModelOf
import com.spark.ui.screens.auth.AuthViewModel
import com.spark.ui.screens.generator.GeneratorViewModel
import com.spark.ui.screens.dashboard.DashboardViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    // ViewModels
    viewModelOf(::AuthViewModel)
    viewModelOf(::GeneratorViewModel)
    viewModelOf(::DashboardViewModel)

    // Local Storage
    single { DataStoreManager(androidContext()) }

    single { com.spark.data.network.ProgressSync(get(), get()) }

    single { com.spark.data.network.SparkAIClient(get(), get()) }

    // Networking
    single { AuthInterceptor(get()) }
    
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<AuthInterceptor>())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .build()
    }
    
    single {
        Retrofit.Builder()
            // Important: Change this IP if you test on physical device vs emulator
            // 10.0.2.2 is for Emulator -> PC localhost

            .baseUrl(BuildConfig.SPARK_BACKEND_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    single {
        get<Retrofit>().create(SparkApiService::class.java)
    }
}
