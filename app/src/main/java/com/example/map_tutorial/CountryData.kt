package com.example.map_tutorial

data class CountryData(
    val type: String,
    val features: List<CountryFeature>
)

data class CountryFeature(
    val type: String,
    val id: String?,
    val properties: Properties,
    val geometry: CountryGeometry
)

data class Properties(
    val name: String
)

data class CountryGeometry(
    val type: String,
    val coordinates: List<Any> // Handles both Polygon and MultiPolygon
)
