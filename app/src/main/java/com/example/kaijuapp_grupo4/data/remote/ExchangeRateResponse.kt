package com.example.kaijuapp_grupo4.data.remote

data class ExchangeRateResponse(
    val amount: Double = 1.0,
    val base: String = "USD",
    val date: String = "",
    val rates: Map<String, Double> = emptyMap()
)
