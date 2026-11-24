package com.example.kaijuapp_grupo4.data.remote

import com.example.kaijuapp_grupo4.BuildConfig
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    var baseUrlActual: String = BuildConfig.BASE_URL

    private const val EXCHANGE_RATE_BASE_URL = "https://api.frankfurter.dev/v1/"

    val backendService: BackendApiService
        get() = Retrofit.Builder()
            .baseUrl(baseUrlActual)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApiService::class.java)

    val exchangeRateService: ExchangeRateApiService by lazy {
        Retrofit.Builder()
            .baseUrl(EXCHANGE_RATE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExchangeRateApiService::class.java)
    }
}
