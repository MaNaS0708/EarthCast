package com.example.earthcast.network

data class HourlyForecast(
    val time: String,
    val date: String,
    val temp: Double,
    val weatherCode: Int,
    val precipitation: Double?,
    val uvIndex: Double?,
    val pressure: Double?
)

