package com.example.map_tutorial
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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

        val airplaneIcon = BitmapDescriptorFactory.fromResource(R.drawable.airplane)

        // Fetch and display air traffic data
        fetchAirTrafficData(airplaneIcon)
        val startMarker = LatLng(34.0, 125.3)
        mMap.addMarker(MarkerOptions().position(startMarker).title("Marker in Orlando"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startMarker, 10f))
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
}