package com.example.earthcast.network

import com.example.earthcast.network.DailyForecast
import com.example.earthcast.network.HourlyForecast

data class WeatherData(
    val currentTemp: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val weatherCode: Int,
    val sunrise: String,
    val sunset: String,
    val windSpeed: Double,
    val windDirection: Double,
    val description: String,
    val timezone: String,
    val hourlyForecast: List<HourlyForecast>,
    val dailyForecast: List<DailyForecast>
)

