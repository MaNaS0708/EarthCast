package com.example.earthcast.network

import android.content.Context
import android.util.Log
import com.example.earthcast.database.CityDatabaseHelper
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class GeocodingFetcher(private val context: Context) {

    private val client = OkHttpClient()

    fun fetchAndStoreCity(cityName: String): Boolean {
        Log.d("GeocodingFetcher", "Fetching city: $cityName")
        val url = "https://geocoding-api.open-meteo.com/v1/search?name=$cityName&count=1"

        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GeocodingFetcher", "API call failed: ${response.code}")
                    return false
                }
                val responseBody = response.body?.string() ?: return false
                Log.d("GeocodingFetcher", "API Response: $responseBody")
                val json = JSONObject(responseBody)

                if (json.has("results")) {
                    val results = json.getJSONArray("results")
                    if (results.length() > 0) {
                        val cityObj = results.getJSONObject(0)
                        val name = cityObj.getString("name")
                        val lat = cityObj.getDouble("latitude")
                        val lon = cityObj.getDouble("longitude")

                        CityDatabaseHelper(context).insertCity(name, lat, lon)
                        Log.d("GeocodingFetcher", "Stored city: $name ($lat, $lon)")
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeocodingFetcher", "Error fetching city data", e)
        }
        return false
    }

    fun fetchCitySuggestions(cityName: String): List<City> {
        val list = mutableListOf<City>()
        val url = "https://geocoding-api.open-meteo.com/v1/search?name=$cityName"

        try {
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: return list
                    val json = JSONObject(responseBody)

                    if (json.has("results")) {
                        val results = json.getJSONArray("results")
                        for (i in 0 until results.length()) {
                            val obj = results.getJSONObject(i)
                            val name = obj.getString("name")
                            val region = obj.optString("admin1")
                            val lat = obj.getDouble("latitude")
                            val lon = obj.getDouble("longitude")
                            list.add(City(name, region, lat, lon))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeocodingFetcher", "Error fetching suggestions", e)
        }
        return list
    }

    fun getNearestCity(lat: Double, lon: Double): String? {
        val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "EarthCastApp")
                .build()

            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody.isNullOrEmpty()) {
                    Log.e("GeocodingFetcher", "Reverse geocode failed: ${response.code}")
                    return null
                }

                Log.d("GeocodingFetcher", "Reverse geocode response: $responseBody")
                val json = JSONObject(responseBody)
                if (json.has("address")) {
                    val address = json.getJSONObject("address")
                    return when {
                        address.has("city") -> address.getString("city")
                        address.has("town") -> address.getString("town")
                        address.has("village") -> address.getString("village")
                        address.has("hamlet") -> address.getString("hamlet")
                        else -> null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeocodingFetcher", "Error reverse geocoding", e)
        }

        return null
    }


}
