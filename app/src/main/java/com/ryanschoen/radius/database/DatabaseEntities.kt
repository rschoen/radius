package com.ryanschoen.radius.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ryanschoen.radius.domain.Venue

@Entity
data class DatabaseVenue constructor(
    @PrimaryKey
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val reviews: Int,
    val rating: Double,
    val imageUrl: String,
    val url: String
)

fun List<DatabaseVenue>.asDomainModel(): List<Venue> {
    return map{
        Venue (
            id = it.id,
            name = it.name,
            lat = it.lat,
            lng = it.lng,
            reviews = it.reviews,
            rating = it.rating,
            imageUrl = it.imageUrl,
            url = it.url,
            closed = false
                )
    }
}
