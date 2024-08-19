package com.example.map_tutorial

data class Feature(
    val attributes: Attributes,
)

data class Attributes(
    val OBJECTID: Int?,
    val UTC_DATETIME: String?,
    val F_SCALE: String?,  // Might be nullable or empty
    val LOCATION: String?,
    val COUNTY: String?,
    val STATE: String?,
    val LATITUDE: Double?,
    val LONGITUDE: Double?,
    val COMMENTS: String?
)




