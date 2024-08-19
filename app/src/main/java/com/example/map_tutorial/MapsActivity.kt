package com.example.map_tutorial

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
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.CheckBox


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private var isTornadoLayerVisible = true




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val filterButton: Button = findViewById(R.id.filterButton)
        filterButton.setOnClickListener {
            showFilterBottomSheet()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun showFilterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.filter, null)

        val checkBoxTornado = bottomSheetView.findViewById<CheckBox>(R.id.checkBoxTornado)

        checkBoxTornado.isChecked = isTornadoLayerVisible
        checkBoxTornado.setOnCheckedChangeListener { _, isChecked ->
            isTornadoLayerVisible = isChecked
            if(!isChecked) {
                TornadoManager.stopMarkers()
                System.out.println("Here")
            }
            toggleTornadoLayer()
        }


        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun toggleTornadoLayer() {
            TornadoManager.retMarkers().forEach { marker ->
                marker.isVisible = isTornadoLayerVisible
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true


        // Fetch and display air traffic data
        val startMarker = LatLng(41.49253740,-99.90181310)
        mMap.addMarker(MarkerOptions().position(startMarker).title("Marker in Nebrasks"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startMarker, 4f))
        TornadoManager.getMarkers(mMap)
        toggleTornadoLayer()
    }

}