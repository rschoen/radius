package com.ryanschoen.radius.network

import com.ryanschoen.radius.domain.AddressResult
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class _NetworkValidationAddress(
    val address: NetworkAddress
)

data class NetworkAddress(
    val addressLines: List<String>,
    val locality: String,
    val administrativeArea: String,
    val regionCode: String
)
fun NetworkAddress.asNetworkValidationAddress(): _NetworkValidationAddress {
    return _NetworkValidationAddress(this)
}


fun NetworkAddressResult.asDomainModel(): AddressResult {
    return AddressResult(result.verdict.addressComplete ?: false,
                        result.address.formattedAddress,
                        result.geocode.location.latitude,
                        result.geocode.location.longitude)
}



// JSON class needed to properly decode Google address validation result
// Not needed outside this file
@JsonClass(generateAdapter = true)
data class NetworkAddressResult(
    val result: _NetworkValidationResult
)

// JSON class needed to properly decode Google address validation result
// Not needed outside this file
@JsonClass(generateAdapter = false)
data class _NetworkValidationResult(
    val verdict: _NetworkVerdict,
    val address: _NetworkAddressInformation,
    val geocode: _NetworkGeocode,
)

// JSON class needed to properly decode Google address validation result
// Not needed outside this file
@JsonClass(generateAdapter = false)
data class _NetworkVerdict(
    val addressComplete: Boolean?
)
// JSON class needed to properly decode Google address validation result
// Not needed outside this file
@JsonClass(generateAdapter = false)
data class _NetworkAddressInformation(
    val formattedAddress: String
)
// JSON class needed to properly decode Google address validation result
// Not needed outside this file
@JsonClass(generateAdapter = false)
data class _NetworkGeocode(
    val location: _NetworkLocation
)
// JSON class needed to properly decode Google address validation result
// Not needed outside this file
@JsonClass(generateAdapter = false)
data class _NetworkLocation(
    val latitude: Double,
    val longitude: Double
)