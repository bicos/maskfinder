package com.ravypark.maskfinder.network

import com.ravypark.maskfinder.BuildConfig
import io.reactivex.schedulers.Schedulers
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiManager {

    companion object {

        val instance: ApiManager by lazy {

            val logInterceptor = HttpLoggingInterceptor()

            if (BuildConfig.DEBUG) {
                logInterceptor.level = HttpLoggingInterceptor.Level.BODY
            }

            val httpClientBuilder = OkHttpClient.Builder()
                .connectionSpecs(listOf(ConnectionSpec.COMPATIBLE_TLS))
                .addInterceptor(logInterceptor)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .client(httpClientBuilder.build())
                .build()

            retrofit.create(ApiManager::class.java)
        }
    }

    @GET("stores/json")
    suspend fun getStores(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500
    ): Response

    @GET("sales/json")
    suspend fun getSales(
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 500
    ): Response

    @GET("storesByGeo/json")
    suspend fun getStoresByGeo(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("m") m: Int = 5000
    ): Response

    @GET("storesByAddr/json")
    suspend fun getStoresByAddr(@Query("address") address: String): Response
}