package com.ryanschoen.radius.network

import com.ryanschoen.radius.BuildConfig
import com.ryanschoen.radius.BuildConfig.PLACES_API_KEY
import com.ryanschoen.radius.database.DatabaseVenue
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class RadiusAPIResult(
    val metadata: RadiusMetadata,
    val venues: List<NetworkVenue>,
)

@JsonClass(generateAdapter = true)
data class RadiusMetadata(
    val queryId: String,
    val resultsComplete: Boolean,
)

@JsonClass(generateAdapter = true)
data class NetworkVenue(
    val id: String,
    val name: String,
    val imageUrl: String?,
    val reviews: Int?,
    val rating: Double?,
    val latitude: Double,
    val longitude: Double,
    val timeLastUpdated: Long,
)





fun RadiusAPIResult.asDatabaseModel(): Array<DatabaseVenue> {
    val apiKey = BuildConfig.PLACES_API_KEY
    return venues.map {
        DatabaseVenue(
            id = it.id,
            name = it.name,
            url = "https://www.google.com/maps/search/?api=1&query=123%20main%20st&query_place_id=" + it.id,
            imageUrl = it.imageUrl?.let { it.replace("API_KEY", apiKey) } ?: "",
            reviews = it.reviews ?: 0,
            rating = convertRating(it.rating),
            lat = it.latitude,
            lng = it.longitude,
            distance = 0.0,
            visited = false,
            hidden = false,
            active = true,
            lastUserUpdate = 0
        )
    }.toTypedArray()
}

fun convertRating(rating: Double?): Double {
    rating?.let {
        return rating*rating/5
    }
    return 0.0

}