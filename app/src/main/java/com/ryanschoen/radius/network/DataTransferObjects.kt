package com.ryanschoen.radius.network

import com.ryanschoen.radius.domain.AddressResult
import com.ryanschoen.radius.domain.Venue
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NetworkVenueContainer(val venues: List<NetworkVenue>)

@JsonClass(generateAdapter = true)
data class NetworkVenue(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val reviews: Int,
    val rating: Double,
    val imageUrl: String
)

fun NetworkVenueContainer.asDomainModel(): List<Venue> {
    return venues.map {
        Venue (
            id = it.id,
            name = it.name,
            address = it.address,
            lat = it.lat,
            lng = it.lng,
            reviews = it.reviews,
            rating = it.rating,
            imageUrl = it.imageUrl
        )
    }
}

@JsonClass(generateAdapter = true)
data class NetworkValidationAddress(
    val address: NetworkAddress
)

data class NetworkAddress(
    val addressLines: List<String>,
    val locality: String,
    val administrativeArea: String,
    val regionCode: String
)
fun NetworkAddress.asNetworkValidationAddress() :NetworkValidationAddress {
    return NetworkValidationAddress(this)
}

@JsonClass(generateAdapter = true)
data class NetworkAddressResult(
    val result: NetworkValidationResult
)

@JsonClass(generateAdapter = true)
data class NetworkValidationResult(
    val verdict: NetworkVerdict,
    val address: NetworkAddressInformation,
    val geocode: NetworkGeocode,
)

@JsonClass(generateAdapter = true)
data class NetworkVerdict(
    val addressComplete: Boolean
)

@JsonClass(generateAdapter = true)
data class NetworkAddressInformation(
    val formattedAddress: String
)

@JsonClass(generateAdapter = true)
data class NetworkGeocode(
    val location: NetworkLocation
)

@JsonClass(generateAdapter = true)
data class NetworkLocation(
    val latitude: Double,
    val longitude: Double
)

fun NetworkAddressResult.asDomainModel(): AddressResult {
    return AddressResult(result.verdict.addressComplete,
                        result.address.formattedAddress,
                        result.geocode.location.latitude,
                        result.geocode.location.longitude)
}