package com.ryanschoen.radius.database

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.database.database
import java.time.Instant

class CloudDatabase() {
    private val database = Firebase.database
    private var userId = ""

    fun setVenueState(venueId: String, visited: Boolean, hidden: Boolean) {
        if(userId.isBlank()) {
            return
        }
        val cloudVenue = CloudVenue(visited, hidden, Instant.now()?.epochSecond?.toInt() ?: 0)
        database.reference.child("users").child(userId).child("venues").child(venueId).setValue(cloudVenue)
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }
}

private lateinit var INSTANCE: CloudDatabase

fun getCloudDatabase(context: Context): CloudDatabase {
    synchronized(VenuesDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = CloudDatabase()
        }
    }
    return INSTANCE
}

data class CloudVenue(val visited: Boolean, val hidden: Boolean, val lastUpdated: Int) {

}

