package com.example.earthcast.network

data class DailyForecast(
    val day: String,
    val maxTemp: Double,
    val minTemp: Double,
    val weatherCode: Int,
    val sunrise: String,
    val sunset: String,
    val date: String
)
