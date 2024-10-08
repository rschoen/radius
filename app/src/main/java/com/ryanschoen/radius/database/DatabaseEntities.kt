package com.ryanschoen.radius.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ryanschoen.radius.domain.Venue
import java.util.Date

@Entity
data class DatabaseVenue (
    @PrimaryKey
    val id: String,
    val name: String,
    val lat: Double?,
    val lng: Double?,
    val reviews: Int,
    val rating: Double,
    val imageUrl: String,
    val url: String,
    var distance: Double,
    var visited: Boolean,
    var hidden: Boolean,
    var active: Boolean,
    @ColumnInfo(defaultValue = "0") var lastUserUpdate: Int
)

fun List<DatabaseVenue>.asDomainModel(): List<Venue> {
    return map {
        Venue(
            id = it.id,
            name = it.name,
            lat = it.lat!!,
            lng = it.lng!!,
            reviews = it.reviews,
            rating = it.rating,
            imageUrl = it.imageUrl,
            url = it.url,
            distance = it.distance,
            visited = it.visited,
            hidden = it.hidden,
            lastUserUpdate = Date(it.lastUserUpdate.toLong() * 1000)
        )
    }
}
