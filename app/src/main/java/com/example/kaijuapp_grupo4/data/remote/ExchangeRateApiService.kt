package com.example.kaijuapp_grupo4.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateApiService {
    @GET("latest")
    suspend fun obtenerTasas(
        @Query("from") base: String = "USD"
    ): ExchangeRateResponse
}
