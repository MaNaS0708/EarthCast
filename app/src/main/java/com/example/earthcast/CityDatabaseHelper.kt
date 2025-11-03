package com.example.earthcast.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.earthcast.network.City

class CityDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "cities.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "cities"
        const val COLUMN_ID = "city_id"
        const val COLUMN_NAME = "city_name"
        const val COLUMN_LAT = "city_lat"
        const val COLUMN_LONG = "city_long"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(
            "CREATE TABLE $TABLE_NAME (" +
                    "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$COLUMN_NAME TEXT UNIQUE, " +   // Name must be unique
                    "$COLUMN_LAT REAL, " +
                    "$COLUMN_LONG REAL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertCity(name: String, lat: Double, lon: Double) {
        val db = writableDatabase
        try {
            db.execSQL(
                "INSERT OR IGNORE INTO $TABLE_NAME ($COLUMN_NAME, $COLUMN_LAT, $COLUMN_LONG) " +
                        "VALUES('$name', $lat, $lon)"
            )
        } finally {
            db.close()
        }
    }

    fun getAllCities(): List<City> {
        val cities = mutableListOf<City>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)

        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LAT))
            val lon = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONG))
            cities.add(City(name, "", lat, lon))
        }

        cursor.close()
        db.close()
        return cities
    }

    fun getAllCitiesList(): List<City> {
        val cityList = mutableListOf<City>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LAT))
            val lon = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONG))
            cityList.add(City(name, "", lat, lon))
        }
        cursor.close()
        db.close()
        return cityList
    }

    fun deleteCity(name: String) {
        val db = writableDatabase
        try {
            db.execSQL("DELETE FROM $TABLE_NAME WHERE $COLUMN_NAME = ?", arrayOf(name))
        } finally {
            db.close()
        }
    }


}
