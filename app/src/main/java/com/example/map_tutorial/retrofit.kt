package com.example.map_tutorial
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("flights")
    fun getAirTraffic(
        @Query("access_key") accessKey: String,
    ): Call<AirTrafficResponse>
}
