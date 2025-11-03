package com.example.earthcast.network

data class Weather(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,

    // Temperature
    val currentTemp: Double,
    val minTemp: Double,
    val maxTemp: Double,

    // Weather codes / icon
    val weatherCode: Int,  // we can map this to icon later
    val weatherDescription: String,

    // Precipitation
    val precipitation: Double,
    val precipitationProbability: Double,

    // Wind
    val windSpeed: Double,
    val windDirection: Double,

    // Sun
    val sunrise: String,
    val sunset: String,

    // UV
    val uvIndex: Double,

    // Air quality (if available)
    val airQualityIndex: Double? = null,
    val pm10: Double? = null,
    val pm2_5: Double? = null,

    // Last updated timestamp
    val lastUpdated: String
)
