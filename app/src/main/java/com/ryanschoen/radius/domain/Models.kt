package com.ryanschoen.radius.domain


data class Venue(val id: String,
                 val name: String,
                 val address: String,
                 val lat: Double,
                 val lng: Double,
                 val reviews: Int,
                 val rating: Double,
                 val imageUrl: String) {
}

data class Address(val address: String,
                val locality: String,
                val administrativeArea: String,
                val regionCode: String = "US")

data class AddressResult(val complete: Boolean,
                         val formattedAddress: String,
                        val latitude: Double,
                        val longitude: Double)
