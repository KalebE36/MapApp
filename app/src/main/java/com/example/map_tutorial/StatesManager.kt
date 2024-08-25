package com.example.map_tutorial

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStream

class StatesManager {companion object {
    private val storedPolygons = mutableListOf<Polygon>()

    fun drawStateBorders(mMap: GoogleMap, context: Context) {
        val json = loadJSONFromRaw(context, R.raw.us_states)

        if (json != null) {
            val gson = Gson()
            val type = object : TypeToken<CountryData>() {}.type
            val featureCollection: CountryData = gson.fromJson(json, type)
            clearPolygons()

            for (feature in featureCollection.features) {
                val countryName = feature.properties.name
                val geometry = feature.geometry

                when (geometry.type) {
                    "Polygon" -> {
                        val coordinates = geometry.coordinates as List<List<List<Double>>>
                        drawPolygon(coordinates, mMap, countryName)
                    }

                    "MultiPolygon" -> {
                        val coordinatesList = geometry.coordinates as List<List<List<List<Double>>>>
                        for (coordinates in coordinatesList) {
                            drawPolygon(coordinates, mMap, countryName)
                        }
                    }
                }
            }
        }
    }


    private fun drawPolygon(
        coordinates: List<List<List<Double>>>,
        mMap: GoogleMap,
        countryName: String
    ) {
        val polygonOptions = PolygonOptions()

        for (coordinateList in coordinates) {
            val polygonPoints = coordinateList.map { LatLng(it[1], it[0]) }
            polygonOptions.addAll(polygonPoints).strokeColor(0xFF000000.toInt()).strokeWidth(5f)
        }

        val polygon = mMap.addPolygon(polygonOptions)
        polygon.tag = countryName // Optional: Tag the polygon with the country name
        storedPolygons.add(polygon)
    }

    fun retPolys(): MutableList<Polygon> {
        return storedPolygons
    }

    fun clearPolygons() {
        storedPolygons.forEach { it.remove() }
        storedPolygons.clear()
    }

    private fun loadJSONFromRaw(context: Context, resourceId: Int): String? {
        return try {
            val inputStream: InputStream = context.resources.openRawResource(resourceId)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            String(buffer, Charsets.UTF_8)
        } catch (ex: Exception) {
            ex.printStackTrace()
            null
        }
    }
}
}

