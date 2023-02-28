package com.ryanschoen.radius.network

import com.ryanschoen.radius.domain.Venue
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class NetworkYelpSearchResults(
    val businesses: List<_YelpBusiness>
)

@JsonClass(generateAdapter = false)
data class _YelpBusiness(
    val id: String,
    val name: String,
    @Json(name="image_url") val imageUrl: String,
    @Json(name="is_closed") val closed: Boolean,
    val url: String,
    @Json(name="review_count") val reviews: String,
    val rating: String,
    val coordinates: _YelpCoordinates,

)

@JsonClass(generateAdapter = false)
data class _YelpCoordinates(
    val latitude: String,
    val longitude: String
)

fun NetworkYelpSearchResults.asDomainModel(): List<Venue> {
    return businesses.map {
        Venue (
            id = it.id,
            name = it.name,
            lat = it.coordinates.latitude.toDouble(),
            lng = it.coordinates.longitude.toDouble(),
            reviews = it.reviews.toInt(),
            rating = it.rating.toDouble(),
            imageUrl = it.imageUrl,
            url = it.url,
            closed = it.closed
        )
    }
}