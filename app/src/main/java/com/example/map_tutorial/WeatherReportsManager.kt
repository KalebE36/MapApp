package com.example.map_tutorial

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException


class WeatherReportsManager {
    companion object {

        private var colorMap: HashMap<String, String> = hashMapOf()
        private val client = OkHttpClient()
        private val gson = Gson()
        private val storedFeatures = mutableListOf<Feature>()
        private var storedUniqueId : UniqueIdField? = null

        private var weatherLoadJob : Job? = null
        private val storedPolygons = mutableListOf<Polygon>()

        fun stopWeatherReport() {
            weatherLoadJob?.cancel()
        }

        fun startWeather(mMap: GoogleMap, context: Context) {
            startWeatherReport(mMap, context)
        }

        fun retPolys() : MutableList<Polygon>{
            return storedPolygons
        }

        private fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }

        private fun getData(mMap: GoogleMap, uiHandler: Handler, context: Context) {
            val url = "https://services9.arcgis.com/RHVPKKiFTONKtxq3/ArcGIS/rest/services/NWS_Watches_Warnings_v1/FeatureServer/6/query?where=Severity+LIKE+%27Extreme%25%27+OR+Severity+LIKE+%27Severe%25%27&outFields=OBJECTID%2C+event%2C+uid&returnGeometry=true&outSR=4326&f=pjson"
            val request = Request.Builder().url(url).build()

            client.newCall(request).enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
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
                            val type = object : TypeToken<WeatherData>() {}.type
                            val weatherData: WeatherData = gson.fromJson(responseBody, type)
                            storedFeatures.clear()
                            storedPolygons.clear()
                            storedFeatures.addAll(weatherData.features)
                            uiHandler.post {
                                drawPolygonsOnMap(mMap, context)
                            }
                        }
                    }
                }
            })
        }

        fun drawPolygonsOnMap(mMap: GoogleMap, context: Context) {
            val name = storedUniqueId?.name
            for (feature in storedFeatures) {
                val geometry = feature.geometry
                val attributes: Attributes = feature.attributes

                val polygonOptions = PolygonOptions()

                for (ring in geometry.rings) {
                    val polygonPoints = ring.map { LatLng(it[1], it[0]) }
                    polygonOptions.addAll(polygonPoints)
                }


                colorMap[attributes.Event]?.let {
                    val color = it.toInt(16) or (0xFF shl 24) 
                    polygonOptions.strokeColor(color).fillColor(color).clickable(true)
                }

                val polygon = mMap.addPolygon(polygonOptions)
                val polygonInfo =
                        "Event: ${attributes.Event}\n " +
                        "UID: ${attributes.Uid}\n" +
                                ""
                polygon.tag = polygonInfo

            }
            mMap.setOnPolygonClickListener { clickedPolygon ->
                val info = clickedPolygon.tag as? String
                info?.let {
                    AlertDialog.Builder(context)
                        .setTitle("Polygon Information")
                        .setMessage(it)
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }

        private fun startWeatherReport(mMap: GoogleMap, context: Context) {
            val uiHandler = Handler(Looper.getMainLooper())

            weatherLoadJob = MainScope().launch {
                while(true) {
                    CoroutineScope(Dispatchers.IO).launch {
                        getData(mMap, uiHandler, context)
                    }
                    delay(1000 * 60 * 10)
                }
            }
        }


        fun initColorHashMap() {
            colorMap["Tsunami Warning"] = "FD6347"
            colorMap["Tornado Warning"] = "FF0000"
            colorMap["Extreme Wind Warning"] = "FF8C00"
            colorMap["Severe Thunderstorm Warning"] = "FFA500"
            colorMap["Flash Flood Warning"] = "8B0000"
            colorMap["Flash Flood Statement"] = "8B0000"
            colorMap["Severe Weather Statement"] = "00FFFF"
            colorMap["Shelter In Place Warning"] = "FA8072"
            colorMap["Evacuation Immediate"] = "7FFF00"
            colorMap["Civil Danger Warning"] = "FFB6C1"
            colorMap["Nuclear Power Plant Warning"] = "4B0082"
            colorMap["Radiological Hazard Warning"] = "4B0082"
            colorMap["Hazardous Materials Warning"] = "4B0082"
            colorMap["Fire Warning"] = "A0522D"
            colorMap["Civil Emergency Message"] = "FFB6C1"
            colorMap["Law Enforcement Warning"] = "C0C0C0"
            colorMap["Storm Surge Warning"] = "B524F7"
            colorMap["Hurricane Force Wind Warning"] = "CD5C5C"
            colorMap["Hurricane Warning"] = "DC143C"
            colorMap["Typhoon Warning"] = "DC143C"
            colorMap["Special Marine Warning"] = "FFA500"
            colorMap["Blizzard Warning"] = "FF4500"
            colorMap["Snow Squall Warning"] = "C71585"
            colorMap["Ice Storm Warning"] = "8B008B"
            colorMap["Winter Storm Warning"] = "FF69B4"
            colorMap["High Wind Warning"] = "DAA520"
            colorMap["Tropical Storm Warning"] = "B22222"
            colorMap["Storm Warning"] = "9400D3"
            colorMap["Tsunami Advisory"] = "D2691E"
            colorMap["Tsunami Watch"] = "FF00FF"
            colorMap["Avalanche Warning"] = "1E90FF"
            colorMap["Earthquake Warning"] = "8B4513"
            colorMap["Volcano Warning"] = "2F4F4F"
            colorMap["Ashfall Warning"] = "A9A9A9"
            colorMap["Coastal Flood Warning"] = "228B22"
            colorMap["Lakeshore Flood Warning"] = "228B22"
            colorMap["Flood Warning"] = "00FF00"
            colorMap["High Surf Warning"] = "228B22"
            colorMap["Dust Storm Warning"] = "FFE4C4"
            colorMap["Blowing Dust Warning"] = "FFE4C4"
            colorMap["Lake Effect Snow Warning"] = "008B8B"
            colorMap["Excessive Heat Warning"] = "C71585"
            colorMap["Tornado Watch"] = "FFFF00"
            colorMap["Severe Thunderstorm Watch"] = "DB7093"
            colorMap["Flash Flood Watch"] = "2E8B57"
            colorMap["Gale Warning"] = "DDA0DD"
            colorMap["Flood Statement"] = "00FF00"
            colorMap["Wind Chill Warning"] = "B0C4DE"
            colorMap["Extreme Cold Warning"] = "0000FF"
            colorMap["Hard Freeze Warning"] = "9400D3"
            colorMap["Freeze Warning"] = "483D8B"
            colorMap["Red Flag Warning"] = "FF1493"
            colorMap["Storm Surge Watch"] = "DB7FF7"
            colorMap["Hurricane Watch"] = "FF00FF"
            colorMap["Hurricane Force Wind Watch"] = "9932CC"
            colorMap["Typhoon Watch"] = "FF00FF"
            colorMap["Tropical Storm Watch"] = "F08080"
            colorMap["Storm Watch"] = "FFE4B5"
            colorMap["Hurricane Local Statement"] = "FFE4B5"
            colorMap["Typhoon Local Statement"] = "FFE4B5"
            colorMap["Tropical Storm Local Statement"] = "FFE4B5"
            colorMap["Tropical Depression Local Statement"] = "FFE4B5"
            colorMap["Avalanche Advisory"] = "CD853F"
            colorMap["Winter Weather Advisory"] = "7B68EE"
            colorMap["Wind Chill Advisory"] = "AFEEEE"
            colorMap["Heat Advisory"] = "FF7F50"
            colorMap["Urban and Small Stream Flood Advisory"] = "00FF7F"
            colorMap["Small Stream Flood Advisory"] = "00FF7F"
            colorMap["Arroyo and Small Stream Flood Advisory"] = "00FF7F"
            colorMap["Flood Advisory"] = "00FF7F"
            colorMap["Hydrologic Advisory"] = "00FF7F"
            colorMap["Lakeshore Flood Advisory"] = "7CFC00"
            colorMap["Coastal Flood Advisory"] = "7CFC00"
            colorMap["High Surf Advisory"] = "BA55D3"
            colorMap["Heavy Freezing Spray Warning"] = "00BFFF"
            colorMap["Dense Fog Advisory"] = "708090"
            colorMap["Dense Smoke Advisory"] = "F0E68C"
            colorMap["Small Craft Advisory"] = "D8BFD8"
            colorMap["Brisk Wind Advisory"] = "D8BFD8"
            colorMap["Hazardous Seas Warning"] = "D8BFD8"
            colorMap["Dust Advisory"] = "BDB76B"
            colorMap["Blowing Dust Advisory"] = "BDB76B"
            colorMap["Lake Wind Advisory"] = "D2B48C"
            colorMap["Wind Advisory"] = "D2B48C"
            colorMap["Frost Advisory"] = "6495ED"
            colorMap["Ashfall Advisory"] = "696969"
            colorMap["Freezing Fog Advisory"] = "008080"
            colorMap["Freezing Spray Advisory"] = "00BFFF"
            colorMap["Low Water Advisory"] = "A52A2A"
            colorMap["Local Area Emergency"] = "C0C0C0"
            colorMap["Avalanche Watch"] = "F4A460"
            colorMap["Blizzard Watch"] = "ADFF2F"
            colorMap["Rip Current Statement"] = "40E0D0"
            colorMap["Beach Hazards Statement"] = "40E0D0"
            colorMap["Gale Watch"] = "FFC0CB"
            colorMap["Winter Storm Watch"] = "4682B4"
            colorMap["Hazardous Seas Watch"] = "483D8B"
            colorMap["Heavy Freezing Spray Watch"] = "BC8F8F"
            colorMap["Coastal Flood Watch"] = "66CDAA"
            colorMap["Lakeshore Flood Watch"] = "66CDAA"
            colorMap["Flood Watch"] = "2E8B57"
            colorMap["High Wind Watch"] = "B8860B"
            colorMap["Excessive Heat Watch"] = "800000"
            colorMap["Extreme Cold Watch"] = "0000FF"
            colorMap["Wind Chill Watch"] = "5F9EA0"
            colorMap["Lake Effect Snow Watch"] = "87CEFA"
            colorMap["Hard Freeze Watch"] = "4169E1"
            colorMap["Freeze Watch"] = "00FFFF"
            colorMap["Fire Weather Watch"] = "FFDEAD"
            colorMap["Extreme Fire Danger"] = "E9967A"
            colorMap["911 Telephone Outage"] = "C0C0C0"
            colorMap["Coastal Flood Statement"] = "6B8E23"
            colorMap["Lakeshore Flood Statement"] = "6B8E23"
            colorMap["Special Weather Statement"] = "FFE4B5"
            colorMap["Marine Weather Statement"] = "FFDAB9"
            colorMap["Air Quality Alert"] = "808080"
            colorMap["Air Stagnation Advisory"] = "808080"
            colorMap["Hazardous Weather Outlook"] = "EEE8AA"
            colorMap["Hydrologic Outlook"] = "90EE90"
            colorMap["Short Term Forecast"] = "98FB98"
            colorMap["Administrative Message"] = "C0C0C0"
            colorMap["Test"] = "F0FFFF"
            colorMap["Child Abduction Emergency"] = "FFFFFF"
            colorMap["Blue Alert"] = "FFFFFF"
        }

    }
}