package com.example.earthcast.network

import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.earthcast.R
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class WeatherFetcher(private val context: Context) {

    private val client = OkHttpClient()

    fun fetchWeather(lat: Double, lon: Double): WeatherData? {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true" +
                "&hourly=temperature_2m,weathercode,windspeed_10m,winddirection_10m,pressure_msl,uv_index,visibility,precipitation_probability" +
                "&daily=temperature_2m_max,temperature_2m_min,weathercode,sunrise,sunset,uv_index_max,precipitation_sum,pressure_msl_mean" +
                "&timezone=auto"

        return try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("WeatherFetcher", "Failed: ${response.code}")
                    return null
                }

                val body = response.body?.string() ?: return null
                val json = JSONObject(body)
                val timezoneStr = json.optString("timezone", "UTC")
                val tz = TimeZone.getTimeZone(timezoneStr)

                val currentObj = json.getJSONObject("current_weather")
                val currentTemp = currentObj.getDouble("temperature")
                val weatherCode = currentObj.getInt("weathercode")
                val windSpeed = currentObj.optDouble("windspeed")
                val windDirection = currentObj.optDouble("winddirection")

                val dailyObj = json.getJSONObject("daily")
                val dailyMaxTemps = dailyObj.getJSONArray("temperature_2m_max")
                val dailyMinTemps = dailyObj.getJSONArray("temperature_2m_min")
                val dailyCodes = dailyObj.getJSONArray("weathercode")
                val dailySunrise = dailyObj.getJSONArray("sunrise")
                val dailySunset = dailyObj.getJSONArray("sunset")
                val dailyTimes = dailyObj.getJSONArray("time")

                val hourlyObj = json.getJSONObject("hourly")
                val hourlyTemps = hourlyObj.getJSONArray("temperature_2m")
                val hourlyCodes = hourlyObj.getJSONArray("weathercode")
                val hourlyTimes = hourlyObj.getJSONArray("time")
                val hourlyPrecip = hourlyObj.optJSONArray("precipitation_probability")
                val hourlyUV = hourlyObj.optJSONArray("uv_index")
                val hourlyVisibility = hourlyObj.optJSONArray("visibility")
                val hourlyPressure = hourlyObj.optJSONArray("pressure_msl")

                val hourlyList = mutableListOf<HourlyForecast>()
                val now = Calendar.getInstance(tz).timeInMillis

                val parseFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
                parseFormat.timeZone = tz
                val hourFormat = SimpleDateFormat("HH", Locale.getDefault())
                hourFormat.timeZone = tz
                val dayMonthFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                dayMonthFormat.timeZone = tz

                for (i in 0 until hourlyTemps.length()) {
                    val timeStr = hourlyTimes.getString(i)
                    val date = parseFormat.parse(timeStr)?.time ?: continue
                    if (date < now) continue

                    val temp = hourlyTemps.getDouble(i)
                    val code = hourlyCodes.getInt(i)
                    val hour = hourFormat.format(date)
                    val dayMonth = dayMonthFormat.format(date)
                    val precipitation = hourlyPrecip?.optDouble(i)
                    val uvIndex = hourlyUV?.optDouble(i)
                    val visibility = hourlyVisibility?.optDouble(i)
                    val pressure = hourlyPressure?.optDouble(i)

                    hourlyList.add(
                        HourlyForecast(
                            time = hour,
                            date = dayMonth,
                            temp = temp,
                            weatherCode = code,
                            precipitation = precipitation,
                            uvIndex = uvIndex,
                            pressure = pressure
                        )
                    )

                    if (hourlyList.size >= 48) break
                }

                val dailyList = mutableListOf<DailyForecast>()
                val dayParse = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                dayParse.timeZone = tz
                val dateFormatted = SimpleDateFormat("dd/MM", Locale.getDefault())
                dateFormatted.timeZone = tz
                val dayNameFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                dayNameFormat.timeZone = tz

                for (i in 0 until dailyMaxTemps.length()) {
                    val max = dailyMaxTemps.getDouble(i)
                    val min = dailyMinTemps.getDouble(i)
                    val code = dailyCodes.getInt(i)
                    val sunriseStr = dailySunrise.getString(i)
                    val sunsetStr = dailySunset.getString(i)
                    val timeStr = dailyTimes.getString(i)
                    val parsedDate = dayParse.parse(timeStr)!!

                    dailyList.add(
                        DailyForecast(
                            day = dayNameFormat.format(parsedDate),
                            maxTemp = max,
                            minTemp = min,
                            weatherCode = code,
                            sunrise = sunriseStr,
                            sunset = sunsetStr,
                            date = dateFormatted.format(parsedDate)
                        )
                    )
                }

                val description = getWeatherDescription(weatherCode)
                WeatherData(
                    currentTemp = currentTemp,
                    minTemp = dailyMinTemps.getDouble(0),
                    maxTemp = dailyMaxTemps.getDouble(0),
                    weatherCode = weatherCode,
                    sunrise = dailySunrise.getString(0),
                    sunset = dailySunset.getString(0),
                    windSpeed = windSpeed,
                    windDirection = windDirection,
                    timezone = timezoneStr,
                    description = description,
                    hourlyForecast = hourlyList,
                    dailyForecast = dailyList
                )
            }
        } catch (e: Exception) {
            Log.e("WeatherFetcher", "Error fetching weather", e)
            null
        }
    }

    fun getWeatherIcon(weatherCode: Int, isDayTime: Boolean = true): Int {
        return when (weatherCode) {
            0 -> if (isDayTime) R.drawable.sun else R.drawable.night
            1, 2, 3 -> if (isDayTime) R.drawable.cloudy else R.drawable.night_overcast
            45, 48 -> R.drawable.foggy
            51, 53, 55 -> R.drawable.drizzle
            61, 63, 65 -> R.drawable.rain
            66, 67 -> R.drawable.freezing_rain
            71, 73, 75 -> R.drawable.snow
            77 -> R.drawable.snow_grains
            80, 81, 82 -> R.drawable.drizzle
            85, 86 -> R.drawable.snow_showers
            95 -> R.drawable.thunder
            96, 99 -> R.drawable.hail
            else -> R.drawable.empty
        }
    }

    fun getWeatherDescription(weatherCode: Int): String {
        return when (weatherCode) {
            0 -> "Clear sky"
            1, 2, 3 -> "Partly cloudy"
            45, 48 -> "Fog"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain"
            66, 67 -> "Freezing rain"
            71, 73, 75 -> "Snow"
            77 -> "Snow grains"
            80, 81, 82 -> "Rain showers"
            85, 86 -> "Snow showers"
            95 -> "Thunderstorm"
            96, 99 -> "Thunderstorm with hail"
            else -> "Unknown"
        }
    }

    fun isDayTime(sunrise: String?, sunset: String?, timezone: String = "UTC"): Boolean {
        return try {
            if (sunrise == null || sunset == null) return true
            val tz = TimeZone.getTimeZone(timezone)
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            format.timeZone = tz

            val sunriseTime = format.parse(sunrise)
            val sunsetTime = format.parse(sunset)
            val now = Calendar.getInstance(tz).time

            sunriseTime != null && sunsetTime != null && now.after(sunriseTime) && now.before(sunsetTime)
        } catch (e: Exception) {
            true
        }
    }

    fun updateWidget(widgetView: CardView, cityName: String, weather: WeatherData) {
        val weatherIcon = widgetView.findViewById<ImageView>(R.id.weatherIcon)
        val cityNameText = widgetView.findViewById<TextView>(R.id.cityName)
        val currentTempText = widgetView.findViewById<TextView>(R.id.currentWeatherText)
        val tempRangeText = widgetView.findViewById<TextView>(R.id.tempRange)

        cityNameText.text = cityName
        currentTempText.text = "${weather.currentTemp}°C"
        tempRangeText.text = "Max: ${weather.maxTemp}°C  Min: ${weather.minTemp}°C"

        val isDayTime = isDayTime(weather.sunrise, weather.sunset, weather.timezone)
        val iconRes = getWeatherIcon(weather.weatherCode, isDayTime)
        weatherIcon.setImageResource(iconRes)
    }

    fun parseHourToMillis(hourStr: String, dateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("dd/MM HH", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC") // or city's timezone if available
            sdf.parse("$dateStr $hourStr")?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}
