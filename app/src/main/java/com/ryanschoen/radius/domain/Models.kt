package com.ryanschoen.radius.domain

import java.util.Date


data class Venue(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val reviews: Int,
    val rating: Double,
    val imageUrl: String,
    val url: String,
    val distance: Double,
    var visited: Boolean,
    val hidden: Boolean,
    var lastUserUpdate: Date
)