package com.example.map_tutorial

import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.*


class TornadoManager {
    companion object {
        private val client = OkHttpClient()
        private var tornadoLoadJob: Job? = null
        private var tornadoMarkers = mutableListOf<Marker>()

        private fun getData(mMap: GoogleMap, uiHandler: Handler) {
            val url = "https://services9.arcgis.com/RHVPKKiFTONKtxq3/arcgis/rest/services/NOAA_storm_reports_v1/FeatureServer/3/query?where=1%3D1&outFields=*&f=json"

            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    // Handle the error
                    uiHandler.post {
                        println("Failed to fetch data: ${e.message}")
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    response.use {
                        if (!it.isSuccessful) {
                            uiHandler.post {
                                println("Unexpected code $response")
                            }
                            return
                        }

                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val jsonResponse = JSONObject(responseBody)
                            val features = jsonResponse.getJSONArray("features")

                            uiHandler.post {
                                for (i in 0 until features.length()) {
                                    val feature = features.getJSONObject(i)
                                    val attributes = feature.getJSONObject("attributes")

                                    val latitude = attributes.optDouble("LATITUDE", 0.0)
                                    val longitude = attributes.optDouble("LONGITUDE", 0.0)
                                    val location = attributes.optString("LOCATION", "Unknown Location")

                                    if (latitude != 0.0 && longitude != 0.0) {
                                        val position = LatLng(latitude, longitude)
                                        val marker = mMap.addMarker(MarkerOptions().position(position).title(location))
                                        marker?.let {
                                            tornadoMarkers.add(it)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            })
        }

        private fun startTornado(mMap: GoogleMap, ) {
            val uiHandler = Handler(Looper.getMainLooper())
            tornadoLoadJob = MainScope().launch {
                while(true) {
                    CoroutineScope(Dispatchers.IO).launch {
                        getData(mMap, uiHandler)
                    }
                    delay(1000 * 60 * 10)
                }

            }
        }

        private fun stopTornado() {
            tornadoLoadJob?.cancel()
        }

        fun getMarkers(mMap: GoogleMap) {
            startTornado(mMap)
        }

        fun stopMarkers() {
            stopTornado()
        }

        fun retMarkers() : MutableList<Marker>{
            return tornadoMarkers
        }

    }
}
