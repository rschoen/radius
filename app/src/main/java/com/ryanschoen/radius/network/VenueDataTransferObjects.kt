package com.ryanschoen.radius.network

import com.ryanschoen.radius.BuildConfig.PLACES_API_KEY
import com.ryanschoen.radius.database.DatabaseVenue
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class NetworkSearchResults(
    val results: List<NetworkVenue>,
    @Json(name="next_page_token") val nextPageToken: String?
)

@JsonClass(generateAdapter = true)
data class NetworkVenue(
    @Json(name="place_id") val id: String,
    val name: String,
    @Json(name="business_status") val businessStatus: String?,
    val photos: List<GooglePhoto>?,
    @Json(name="user_ratings_total") val reviews: Int?,
    val geometry: GoogleGeometry,
    val rating: Double?
)

@JsonClass(generateAdapter = true)
data class GoogleGeometry(
    val location: GoogleLocation
)

@JsonClass(generateAdapter = true)
data class GoogleLocation(
    val lat: Double,
    val lng: Double
)

@JsonClass(generateAdapter = true)
data class GooglePhoto (
    @Json(name="photo_reference") val photoReference: String
)



fun NetworkSearchResults.asDatabaseModel(): Array<DatabaseVenue> {
    return results.map {
        DatabaseVenue(
            id = it.id,
            name = it.name,
            url = "https://www.google.com/maps/search/?api=1&query=123%20main%20st&query_place_id=" + it.id,
            imageUrl = photoListToURL(it.photos),
            reviews = it.reviews ?: 0,
            rating = convertRating(it.rating),
            lat = it.geometry.location.lat,
            lng = it.geometry.location.lng,
            distance = 0.0,
            visited = false,
            hidden = false,
            active = true,
            lastUserUpdate = 0
        )
    }.toTypedArray()
}

fun photoListToURL(photoList: List<GooglePhoto>?): String {
    if(!photoList.isNullOrEmpty()) {
        val photoRef = photoList[0].photoReference
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=70&photo_reference=${photoRef}&key=${PLACES_API_KEY}"
    } else {
        return ""
    }
}

fun convertRating(rating: Double?): Double {
    rating?.let {
        return rating*rating/5
    }
    return 0.0

}