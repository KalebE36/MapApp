package com.example.map_tutorial
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object arcgisRetrofit {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://services9.arcgis.com/RHVPKKiFTONKtxq3/arcgis/rest/services/NOAA_storm_reports_v1/FeatureServer/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: arcgis by lazy {
        retrofit.create(arcgis::class.java)
    }
}
