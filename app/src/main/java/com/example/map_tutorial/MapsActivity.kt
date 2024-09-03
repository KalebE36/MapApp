package com.example.map_tutorial

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.map_tutorial.databinding.ActivityMapsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.CheckBox


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var isTornadoLayerVisible = false
    private var isWeatherLayerVisible = false
    private var isCountryLayerVisible = false
    private var isCountyLayerVisible = false
    private var isStateLayerVisible = false

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

        isTornadoLayerVisible = sharedPreferences.getBoolean("isTornadoLayerVisible", false)
        isWeatherLayerVisible = sharedPreferences.getBoolean("isWeatherLayerVisible", false)
        isCountryLayerVisible = sharedPreferences.getBoolean("isCountryLayerVisible", false)
        isCountyLayerVisible = sharedPreferences.getBoolean("isCountyLayerVisible", false)
        isStateLayerVisible = sharedPreferences.getBoolean("isStateLayerVisible", false)


        val filterButton: Button = findViewById(R.id.filterButton)
        filterButton.setOnClickListener {
            showFilterBottomSheet()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true

        WeatherReportsManager.initColorHashMap()
        if(isTornadoLayerVisible) {
            TornadoManager.getMarkers(mMap, this)
        }

        if(isWeatherLayerVisible) {
            WeatherReportsManager.startWeather(mMap, this)
        }

        if(isCountryLayerVisible) {
            CountryManager.drawCountryBorders(mMap, this)
        }

        if(isCountyLayerVisible) {
            CountyManager.drawCountyBorders(mMap, this)
        }

        if(isStateLayerVisible) {
            StatesManager.drawStateBorders(mMap, this)
        }


        val startMarker = LatLng(41.49253740,-99.90181310)
        mMap.addMarker(MarkerOptions().position(startMarker).title("Marker in Nebraska"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startMarker, 4f))
        toggleWeatherLayer()
        toggleTornadoLayer()
        toggleCountryLayer()
        toggleCountyLayer()
        toggleStateLayer()
    }

    private fun showFilterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.filter, null)

        val checkBoxTornado = bottomSheetView.findViewById<CheckBox>(R.id.checkBoxTornado)
        val checkBoxWeather = bottomSheetView.findViewById<CheckBox>(R.id.checkBoxWeather)
        val checkBoxCountry = bottomSheetView.findViewById<CheckBox>(R.id.checkBoxCountry)
        val checkBoxCounty = bottomSheetView.findViewById<CheckBox>(R.id.checkBoxCounty)
        val checkBoxState = bottomSheetView.findViewById<CheckBox>(R.id.checkBoxState)


        checkBoxTornado.isChecked = isTornadoLayerVisible
        checkBoxTornado.setOnCheckedChangeListener { _, isChecked ->
            isTornadoLayerVisible = isChecked
            savePreferences("isTornadoLayerVisible", isTornadoLayerVisible)

            if(!isChecked) {
                TornadoManager.stopMarkers()
            } else {
                TornadoManager.getMarkers(mMap, this)
            }
            toggleTornadoLayer()
        }

        checkBoxWeather.isChecked = isWeatherLayerVisible
        checkBoxWeather.setOnCheckedChangeListener {_, isChecked ->
            isWeatherLayerVisible = isChecked
            savePreferences("isWeatherLayerVisible", isWeatherLayerVisible)
            if(!isChecked) {
                WeatherReportsManager.stopWeatherReport()
            } else {
                WeatherReportsManager.startWeather(mMap, this)
            }
            toggleWeatherLayer()
        }

        checkBoxCountry.isChecked = isCountryLayerVisible
        checkBoxCountry.setOnCheckedChangeListener {_, isChecked ->
            isCountryLayerVisible = isChecked
            savePreferences("isCountryLayerVisible", isCountryLayerVisible)
            if(isChecked) {
               CountryManager.drawCountryBorders(mMap, this)
            }
            toggleCountryLayer()
        }

        checkBoxCounty.isChecked = isCountyLayerVisible
        checkBoxCounty.setOnCheckedChangeListener {_, isChecked ->
            isCountyLayerVisible = isChecked
            savePreferences("isCountyLayerVisible", isCountyLayerVisible)
            if(isChecked) {
                CountyManager.drawCountyBorders(mMap, this)
            }
            toggleCountyLayer()
        }

        checkBoxState.isChecked = isStateLayerVisible
        checkBoxState.setOnCheckedChangeListener {_, isChecked ->
            isStateLayerVisible = isChecked
            savePreferences("isStateLayerVisible", isStateLayerVisible)
            if(isChecked) {
                StatesManager.drawStateBorders(mMap, this)
            }
            toggleStateLayer()
        }



        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun toggleTornadoLayer() {
            TornadoManager.retMarkers().forEach { marker ->
                marker.isVisible = isTornadoLayerVisible
            }
    }

    private fun toggleWeatherLayer() {
        WeatherReportsManager.retPolys().forEach { poly ->
            poly.isVisible = isWeatherLayerVisible
        }
    }

    private fun toggleCountryLayer() {
        CountryManager.retPolys().forEach { poly ->
            poly.isVisible = isCountryLayerVisible
        }
    }

    private fun toggleCountyLayer() {
        CountyManager.retPolys().forEach { poly ->
            poly.isVisible = isCountyLayerVisible
        }
    }

    private fun toggleStateLayer() {
        StatesManager.retPolys().forEach { poly ->
            poly.isVisible = isStateLayerVisible
        }
    }



    private fun savePreferences(key: String, value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }



}