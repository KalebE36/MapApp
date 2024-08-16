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
import com.google.android.gms.maps.model.BitmapDescriptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.CheckBox


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val tornadoMarkers = mutableListOf<Marker>()
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
            toggleTornadoLayer()
        }


        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun toggleTornadoLayer() {
        tornadoMarkers.forEach { marker ->
            marker.isVisible = isTornadoLayerVisible
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true

        val airplaneIcon = BitmapDescriptorFactory.fromResource(R.drawable.airplane)
        val tornadoIcon = BitmapDescriptorFactory.fromResource(R.drawable.tornado)

        // Fetch and display air traffic data
        fetchAirTrafficData(airplaneIcon)
        fetchStormReports(tornadoIcon)
        val startMarker = LatLng(34.0, 125.3)
        mMap.addMarker(MarkerOptions().position(startMarker).title("Marker in Orlando"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startMarker, 10f))
        toggleTornadoLayer()
    }

    private fun fetchAirTrafficData(airplaneIcon: BitmapDescriptor) {
        val apiKey = "27bac14c9304a0ff33d73717d65e9817"

        RetrofitInstance.api.getAirTraffic(apiKey).enqueue(object : Callback<AirTrafficResponse> {
            override fun onResponse(call: Call<AirTrafficResponse>, response: Response<AirTrafficResponse>) {
                if (response.isSuccessful) {
                    val flightDataList = response.body()?.data

                    flightDataList?.let { flights ->
                        for (flight in flights) {
                            val live = flight.live
                            val airline = flight.airline
                            println("Airline:  ${airline?.name}")

                            if (live != null) {
                                println("Live data: ${live.latitude}, ${live.longitude}, ${live.altitude}")
                                val position = LatLng(live.latitude ?: 0.0, live.longitude ?: 0.0)
                                mMap.addMarker(MarkerOptions()
                                    .position(position)
                                    .title("Altitude: ${live.altitude ?: "N/A"} m, Speed: ${live.speed_horizontal ?: "N/A"} km/h")
                                    .icon(airplaneIcon))
                            } else {
                                println("No live data available for this flight.")
                            }
                        }
                    }
                } else {
                    println("Response not successful: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<AirTrafficResponse>, t: Throwable) {
                println("Failed to fetch data: ${t.message}")
            }
        })
    }

    private fun fetchStormReports(tornadoIcon: BitmapDescriptor) {
        arcgisRetrofit.api.getStormReports().enqueue(object : Callback<ArcGisResponse> {
            override fun onResponse(call: Call<ArcGisResponse>, response: Response<ArcGisResponse>) {
                if (response.isSuccessful) {
                    val stormReports = response.body()?.features

                    stormReports?.forEach { feature ->
                        val attributes = feature.attributes

                        println("OBJECTID: ${attributes.OBJECTID}")
                        println("UTC_DATETIME: ${attributes.UTC_DATETIME}")
                        println("F_SCALE: ${attributes.F_SCALE}")
                        println("LOCATION: ${attributes.LOCATION}")
                        println("COUNTY: ${attributes.COUNTY}")
                        println("STATE: ${attributes.STATE}")
                        println("LATITUDE: ${attributes.LATITUDE}")
                        println("LONGITUDE: ${attributes.LONGITUDE}")
                        println("COMMENTS: ${attributes.COMMENTS}")
                        println("--------------------")

                        if(attributes.LATITUDE != null) {
                            val position = LatLng(attributes.LATITUDE ?: 0.0, attributes.LONGITUDE ?: 0.0)
                            val marker = mMap.addMarker(MarkerOptions()
                                .position(position)
                                .title("${attributes.LOCATION}"))

                            marker?.let {
                                tornadoMarkers.add(it)
                            }
                        }
                    }
                } else {
                    println("Response not successful: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ArcGisResponse>, t: Throwable) {
                println("Failed to fetch data: ${t.message}")
            }
        })
    }

}