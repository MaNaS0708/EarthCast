package com.example.earthcast

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class Welcome : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        val start = findViewById<Button>(R.id.start)

        start.setOnClickListener {

            val prefs = getSharedPreferences("EarthCastPrefs", MODE_PRIVATE)
            prefs.edit().putBoolean("isFirstTime", false).apply()

            val intent = Intent(this, Home::class.java)
            startActivity(intent)

            finish()
        }
    }
}
