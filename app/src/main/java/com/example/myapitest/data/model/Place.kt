package com.example.myapitest.data.model

import com.google.gson.annotations.SerializedName

// RZ - Modelo para representar a localização geográfica na API
data class Place(
    @SerializedName("lat") val lat: Double,
    @SerializedName("long") val long: Double
)
