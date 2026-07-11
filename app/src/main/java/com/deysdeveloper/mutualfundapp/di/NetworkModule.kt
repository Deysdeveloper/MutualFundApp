package com.deysdeveloper.mutualfundapp.di

import com.deysdeveloper.mutualfundapp.data.api.MfApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://api.mfapi.in/"
    private const val TIMEOUT_SECONDS = 30L

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "MutualFundApp/1.0")
                    .build()
                
                var response: okhttp3.Response? = null
                var error: Exception? = null
                
                // Retry up to 3 times with a small delay
                for (i in 1..3) {
                    try {
                        response = chain.proceed(request)
                        if (response.isSuccessful) return@addInterceptor response
                    } catch (e: Exception) {
                        error = e
                    }
                    
                    if (i < 3) {
                        Thread.sleep(1000L * i) // Exponential backoff (1s, 2s)
                    }
                }
                
                response ?: throw error ?: Exception("Unknown network error")
            }
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideMfApiService(retrofit: Retrofit): MfApiService =
        retrofit.create(MfApiService::class.java)
}
