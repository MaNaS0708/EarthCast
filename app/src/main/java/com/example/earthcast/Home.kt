package com.example.earthcast

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import com.example.earthcast.database.CityDatabaseHelper
import com.example.earthcast.network.GeocodingFetcher
import com.example.earthcast.network.WeatherData
import com.example.earthcast.network.WeatherFetcher
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class Home : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocodingFetcher: GeocodingFetcher
    private lateinit var weatherFetcher: WeatherFetcher
    private lateinit var dbHelper: CityDatabaseHelper
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        geocodingFetcher = GeocodingFetcher(this)
        weatherFetcher = WeatherFetcher(this)
        dbHelper = CityDatabaseHelper(this)
        container = findViewById(R.id.savedCityContainer)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val addCity = findViewById<FloatingActionButton>(R.id.Search)
        val fabGPS = findViewById<FloatingActionButton>(R.id.fabGPS)
        val currentCityWidget = findViewById<CardView>(R.id.currentCityWidget)

        currentCityWidget.setOnClickListener {
            val sharedPref = getSharedPreferences("CURRENT_CITY", Context.MODE_PRIVATE)
            val cityName = sharedPref.getString("cityName", null)
            val lat = sharedPref.getFloat("latitude", 0f).toDouble()
            val lon = sharedPref.getFloat("longitude", 0f).toDouble()
            if (cityName != null) {
                val intent = Intent(this, Info::class.java)
                intent.putExtra("cityName", cityName)
                intent.putExtra("lat", lat)
                intent.putExtra("lon", lon)
                startActivity(intent)
            }
        }

        addCity.setOnClickListener {
            startActivity(Intent(this, Search::class.java))
        }

        fabGPS.setOnClickListener {
            getLocationAndUpdateCity(currentCityWidget)
        }

        loadCities(currentCityWidget)

        val button = findViewById<ImageButton>(R.id.hamburgerIcon)
        button.setOnClickListener {
            val popup = PopupMenu(this, button)
            popup.menuInflater.inflate(R.menu.context_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.abt -> {
                        val intent = Intent(this, About_Me::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.how -> {
                        val intent = Intent(this, How_Made_It::class.java)
                        startActivity(intent)
                        true
                    }
                    R.id.linkedin -> {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.linkedin.com/in/manasbeniwal"))
                        startActivity(intent)
                        true
                    }
                    else -> false
                }
            }

            popup.show()
        }
    }

    private fun loadCities(currentCityWidget: CardView) {
        container.removeAllViews()

        val sharedPref = getSharedPreferences("CURRENT_CITY", Context.MODE_PRIVATE)
        val currentCityName = sharedPref.getString("cityName", null)
        val currentLat = sharedPref.getFloat("latitude", 0f).toDouble()
        val currentLon = sharedPref.getFloat("longitude", 0f).toDouble()

        if (!currentCityName.isNullOrEmpty()) {
            Thread {
                try {
                    val weather: WeatherData? = weatherFetcher.fetchWeather(currentLat, currentLon)
                    if (weather != null) {
                        runOnUiThread {
                            weatherFetcher.updateWidget(currentCityWidget, currentCityName, weather)
                            updateLastUpdated(currentCityWidget)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Home", "Error fetching current city weather", e)
                }
            }.start()
        }

        val savedCities = dbHelper.getAllCitiesList()
        for (city in savedCities) {
            Thread {
                try {
                    val weather: WeatherData? = weatherFetcher.fetchWeather(city.latitude, city.longitude)
                    if (weather != null) {
                        runOnUiThread {
                            val widget = layoutInflater.inflate(R.layout.city_widgit, container, false)
                            val cityWidget = widget as CardView
                            weatherFetcher.updateWidget(cityWidget, city.name, weather)
                            updateLastUpdated(cityWidget)

                            cityWidget.setOnClickListener {
                                val intent = Intent(this, Info::class.java)
                                intent.putExtra("cityName", city.name)
                                intent.putExtra("lat", city.latitude)
                                intent.putExtra("lon", city.longitude)
                                startActivity(intent)
                            }

                            cityWidget.setOnLongClickListener {
                                androidx.appcompat.app.AlertDialog.Builder(this)
                                    .setTitle("Delete City")
                                    .setMessage("Do you want to delete ${city.name}?")
                                    .setPositiveButton("Delete") { _, _ ->
                                        dbHelper.deleteCity(city.name)
                                        container.removeView(cityWidget)
                                    }
                                    .setNegativeButton("Cancel", null)
                                    .show()
                                true
                            }

                            container.addView(cityWidget)
                        }
                    } else {
                        Log.w("Home", "No weather data for ${city.name}")
                    }
                } catch (e: Exception) {
                    Log.e("Home", "Error fetching saved city weather for ${city.name}", e)
                }
            }.start()
        }
    }

    private fun getLocationAndUpdateCity(widget: CardView) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude

                Thread {
                    try {
                        val cityName = geocodingFetcher.getNearestCity(lat, lon)
                        if (!cityName.isNullOrEmpty()) {

                            val sharedPref = getSharedPreferences("CURRENT_CITY", Context.MODE_PRIVATE)
                            with(sharedPref.edit()) {
                                putString("cityName", cityName)
                                putFloat("latitude", lat.toFloat())
                                putFloat("longitude", lon.toFloat())
                                apply()
                            }

                            val weather: WeatherData? = weatherFetcher.fetchWeather(lat, lon)
                            if (weather != null) {
                                runOnUiThread {
                                    weatherFetcher.updateWidget(widget, cityName, weather)
                                    updateLastUpdated(widget)
                                    Toast.makeText(this, "Location updated: $cityName", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(this, "Could not find nearest city", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Home", "Error updating city from GPS", e)
                    }
                }.start()
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to get location: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLastUpdated(widget: CardView) {
        val lastUpdated = widget.findViewById<TextView>(R.id.lastUpdated)
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        lastUpdated.text = "Updated: $currentTime"
    }
}
