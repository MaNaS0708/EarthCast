package com.example.earthcast

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("EarthCastPrefs", MODE_PRIVATE)
        val isFirstTime = prefs.getBoolean("isFirstTime", true)

        if (isFirstTime) {

            val intent = Intent(this, Welcome::class.java)
            startActivity(intent)
        }
        else {

            val intent = Intent(this, Home::class.java)
            startActivity(intent)
        }

        finish()
    }
}
