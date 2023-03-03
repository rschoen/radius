package com.ryanschoen.radius.domain


data class Venue(val id: String,
                 val name: String,
                 val lat: Double,
                 val lng: Double,
                 val reviews: Int,
                 val rating: Double,
                 val imageUrl: String,
                 val url: String,
                 val distance: Double)
data class AddressResult(val complete: Boolean,
                         val formattedAddress: String,
                        val latitude: Double,
                        val longitude: Double)
