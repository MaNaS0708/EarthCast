package com.example.earthcast

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.earthcast.network.City
import com.example.earthcast.network.GeocodingFetcher

class Search : AppCompatActivity() {

    private lateinit var geocodingFetcher: GeocodingFetcher
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        geocodingFetcher = GeocodingFetcher(this)

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        val scrollView = findViewById<ScrollView>(R.id.recommendationScrollView)
        container = findViewById(R.id.recommendationContainer)

        backButton.setOnClickListener { finish() }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    Thread {
                        try {
                            val suggestions = geocodingFetcher.fetchCitySuggestions(query)
                            runOnUiThread {
                                showSuggestions(suggestions)
                            }
                        } catch (e: Exception) {
                            Log.e("SearchActivity", "Error fetching suggestions", e)
                        }
                    }.start()
                } else {
                    container.removeAllViews()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun showSuggestions(suggestions: List<City>) {
        container.removeAllViews()
        for (city in suggestions) {
            val textView = TextView(this)
            textView.text = "${city.name}, ${city.region}"
            textView.textSize = 18f
            textView.setTextColor(resources.getColor(android.R.color.white, theme))
            textView.setPadding(16, 24, 16, 24)
            textView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            textView.setOnClickListener {
                Thread {
                    try {
                        val isFetched = geocodingFetcher.fetchAndStoreCity(city.name)
                        runOnUiThread {
                            if (isFetched) {
                                Toast.makeText(
                                    this,
                                    "${city.name} fetched and stored in DB",
                                    Toast.LENGTH_SHORT
                                ).show()

                                val intent = Intent(this, Home::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "City not found: ${city.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SearchActivity", "Error fetching city", e)
                        runOnUiThread {
                            Toast.makeText(
                                this,
                                "Error fetching city: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.start()
            }
            container.addView(textView)
        }
    }
}
