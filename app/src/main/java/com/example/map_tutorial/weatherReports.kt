package com.example.map_tutorial

data class WeatherData(
    val objectIdFieldName: String,
    val uniqueIdField: UniqueIdField,
    val globalIdFieldName: String,
    val geometryProperties: GeometryProperties,
    val geometryType: String,
    val spatialReference: SpatialReference,
    val fields: List<Field>,
    val features: List<Feature>
)

data class UniqueIdField(
    val name: String,
    val isSystemMaintained: Boolean
)

data class GeometryProperties(
    val shapeAreaFieldName: String,
    val shapeLengthFieldName: String,
    val units: String
)

data class SpatialReference(
    val wkid: Int,
    val latestWkid: Int
)

data class Field(
    val name: String,
    val type: String,
    val alias: String,
    val sqlType: String,
    val length: Int? = null,
    val domain: Any? = null,
    val defaultValue: Any? = null
)

data class Feature(
    val attributes: Attributes,
    val geometry: Geometry
)

data class Attributes(
    val OBJECTID: Long,
    val Event: String,
    val Uid: String
)

data class Geometry(
    val rings: List<List<List<Double>>>
)



