package com.example.map_tutorial
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface arcgis {
    @GET("query")
    fun getStormReports(
        @Query("where") where: String = "1=1",
        @Query("outFields") outFields: String = "*",
        @Query("f") format: String = "json" // Ensure the response format is JSON
    ): Call<ArcGisResponse>
}