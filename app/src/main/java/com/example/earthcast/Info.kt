package com.example.earthcast

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.earthcast.network.DailyForecast
import com.example.earthcast.network.HourlyForecast
import com.example.earthcast.network.WeatherData
import com.example.earthcast.network.WeatherFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Info : AppCompatActivity() {

    private lateinit var weatherFetcher: WeatherFetcher
    private lateinit var hourlyContainer: LinearLayout
    private lateinit var dailyContainer: LinearLayout

    private lateinit var cityNameText: TextView
    private lateinit var currentTempText: TextView
    private lateinit var minMaxTempText: TextView
    private lateinit var weatherTypeText: TextView
    private lateinit var weatherIconImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_info)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        hourlyContainer = findViewById(R.id.hourlyCardsContainer)
        dailyContainer = findViewById(R.id.dailyCardsContainer)

        cityNameText = findViewById(R.id.cityName)
        currentTempText = findViewById(R.id.temperature)
        minMaxTempText = findViewById(R.id.minMaxTemp)
        weatherTypeText = findViewById(R.id.weatherType)
        weatherIconImage = findViewById(R.id.weatherIcon)

        weatherFetcher = WeatherFetcher(this)

        val lat = intent.getDoubleExtra("lat", 0.0)
        val lon = intent.getDoubleExtra("lon", 0.0)
        val cityName = intent.getStringExtra("cityName") ?: "--"
        cityNameText.text = cityName

        CoroutineScope(Dispatchers.IO).launch {
            val weather: WeatherData? = weatherFetcher.fetchWeather(lat, lon)
            withContext(Dispatchers.Main) {
                weather?.let { data ->
                    currentTempText.text = "${data.currentTemp.toInt()}°C"
                    minMaxTempText.text = "Max ${data.maxTemp.toInt()}°C · Min ${data.minTemp.toInt()}°C"
                    weatherTypeText.text = data.description
                    val isDayTimeMain = weatherFetcher.isDayTime(data.sunrise, data.sunset)
                    weatherIconImage.setImageResource(weatherFetcher.getWeatherIcon(data.weatherCode, isDayTimeMain))

                    populateHourlyCards(data)
                    populateDailyCards(data)
                    populateGridCards(data)
                }
            }
        }
    }

    private fun populateHourlyCards(data: WeatherData) {
        hourlyContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        // Parse sunrise and sunset hours from the data
        val sunriseHour = try {
            data.sunrise.substringAfter("T").substringBefore(":").toInt()
        } catch (e: Exception) { 6 } // fallback if parsing fails

        val sunsetHour = try {
            data.sunset.substringAfter("T").substringBefore(":").toInt()
        } catch (e: Exception) { 18 } // fallback if parsing fails

        for (hourly in data.hourlyForecast) {
            val card = inflater.inflate(R.layout.forecast_hr, hourlyContainer, false)

            // Temperature
            card.findViewById<TextView>(R.id.tempText).text = "${hourly.temp.toInt()}°C"

            // Format hour as "HH:00"
            val hourInt = hourly.time.toIntOrNull() ?: 0
            val formattedTime = "%02d:00".format(hourInt)
            card.findViewById<TextView>(R.id.timeText).text = formattedTime

            // Day text (optional)
            card.findViewById<TextView>(R.id.dayText).text = hourly.date

            // Determine if this hour is day or night based on actual sunrise/sunset
            val isDayTime = hourInt in sunriseHour until sunsetHour

            // Set proper weather icon
            val icon = card.findViewById<ImageView>(R.id.weatherIcon)
            icon.setImageResource(weatherFetcher.getWeatherIcon(hourly.weatherCode, isDayTime))

            hourlyContainer.addView(card)
        }
    }



    private fun populateDailyCards(data: WeatherData) {
        dailyContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)

        for (daily in data.dailyForecast) {
            val card = inflater.inflate(R.layout.forecast_day, dailyContainer, false)

            // Max and Min temperatures
            card.findViewById<TextView>(R.id.MaxTemp).text = "${daily.maxTemp.toInt()}°C"
            card.findViewById<TextView>(R.id.minTemp).text = "${daily.minTemp.toInt()}°C"

            // Day or date
            card.findViewById<TextView>(R.id.dayText).text = daily.day

            // Weather icon (always day)
            val icon = card.findViewById<ImageView>(R.id.weatherIcon)
            icon.setImageResource(weatherFetcher.getWeatherIcon(daily.weatherCode, true))

            dailyContainer.addView(card)
        }
    }



    private fun populateGridCards(data: WeatherData) {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val sunriseTime = try {
            outputFormat.format(inputFormat.parse(data.sunrise) ?: Date())
        } catch (e: Exception) { "--:--" }

        val sunsetTime = try {
            outputFormat.format(inputFormat.parse(data.sunset) ?: Date())
        } catch (e: Exception) { "--:--" }


        val now = System.currentTimeMillis()
        val currentHourForecast = data.hourlyForecast.minByOrNull {
            WeatherFetcher(this).parseHourToMillis(it.time, it.date)
        } ?: data.hourlyForecast.firstOrNull()


        val sunriseCard = findViewById<androidx.cardview.widget.CardView>(R.id.card_sunrise)
        sunriseCard.findViewById<TextView>(R.id.heading).text = "Sunrise"
        sunriseCard.findViewById<TextView>(R.id.info).text = sunriseTime
        sunriseCard.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.sunrise)


        val sunsetCard = findViewById<androidx.cardview.widget.CardView>(R.id.card_sunset)
        sunsetCard.findViewById<TextView>(R.id.heading).text = "Sunset"
        sunsetCard.findViewById<TextView>(R.id.info).text = sunsetTime
        sunsetCard.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.sunset)


        val precipitationCard = findViewById<androidx.cardview.widget.CardView>(R.id.card_precipitation)
        precipitationCard.findViewById<TextView>(R.id.heading).text = "Precipitation"
        precipitationCard.findViewById<TextView>(R.id.info).text = "${currentHourForecast?.precipitation ?: 0.0} mm"
        precipitationCard.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.precipitaiton)


        val windCard = findViewById<androidx.cardview.widget.CardView>(R.id.card_wind)
        windCard.findViewById<TextView>(R.id.speed).text = "${data.windSpeed} km/h"
        windCard.findViewById<TextView>(R.id.dir).text = getWindDirection(data.windDirection)
        val windIcon = windCard.findViewById<ImageView>(R.id.item_Icon)
        windIcon.rotation = data.windDirection.toFloat()

        val uvCard = findViewById<androidx.cardview.widget.CardView>(R.id.card_uv)
        uvCard.findViewById<TextView>(R.id.heading).text = "UV Index"
        uvCard.findViewById<TextView>(R.id.info).text = "${currentHourForecast?.uvIndex ?: 0.0}"
        uvCard.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.uv_protection)


        val pressureCard = findViewById<androidx.cardview.widget.CardView>(R.id.card_pressure)
        pressureCard.findViewById<TextView>(R.id.heading).text = "Pressure"
        pressureCard.findViewById<TextView>(R.id.info).text = "${currentHourForecast?.pressure ?: 0.0} hPa"
        pressureCard.findViewById<ImageView>(R.id.icon).setImageResource(R.drawable.pressure)
    }


    fun getWindDirection(degrees: Double): String {
        return when ((degrees % 360).toInt()) {
            in 0..22 -> "N"
            in 23..67 -> "NE"
            in 68..112 -> "E"
            in 113..157 -> "SE"
            in 158..202 -> "S"
            in 203..247 -> "SW"
            in 248..292 -> "W"
            in 293..337 -> "NW"
            in 338..359 -> "N"
            else -> "--"
        }
    }
}
