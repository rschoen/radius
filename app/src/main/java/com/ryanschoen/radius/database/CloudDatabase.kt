package com.ryanschoen.radius.database

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.ryanschoen.radius.domain.Venue
import timber.log.Timber
import java.time.Instant
import java.util.Date

class CloudDatabase() {
    private val database = Firebase.database
    private var userId = ""
    private var listener: ValueEventListener? = null

    fun setVenueState(venueId: String, visited: Boolean, hidden: Boolean, timestamp: Date) {
        if(userId.isBlank()) {
            return
        }

        // Need to plumb through the timestamp to write onto this if we're not actually updating it to now.
        // Pass through from the underlying venue type?
        // Need to think hard about specifically when it should be updated so we're not causing a loop

        val cloudVenue = CloudVenue(venueId, visited, hidden, timestamp.toInstant().epochSecond.toInt())
        postCloudVenue(cloudVenue)
    }

    fun setUserId(userId: String) {
        this.userId = userId
    }

    fun subscribeToVenueChanges(callback: (List<Venue>) -> Unit) {
        listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                callback(dataSnapshot.children.mapNotNull { it.getValue<CloudVenue>() }.asDomainModel())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Timber.w(databaseError.toException())
            }
        }
        database.reference.child("users").child(userId).child("venues").addValueEventListener(
            listener as ValueEventListener
        )
    }

    fun getOneTimeSnapshot(callback: (List<Venue>) -> Unit) {
        database.reference.child("users").child(userId).child("venues").get().addOnSuccessListener { snapshot ->
            callback(snapshot.children.mapNotNull { it.getValue<CloudVenue>() }.asDomainModel())
        }.addOnFailureListener{
            Timber.w(it)
        }
    }

    fun uploadVenueChanges(venues: List<Venue>) {
        val cloudVenues = venues.asCloudModel()
        for(cloudVenue in cloudVenues) {
            database.reference.child("users").child(userId).child("venues").child(cloudVenue.venueId).get().addOnSuccessListener {
                val storedCloudVenue = it.getValue<CloudVenue>()
                if(storedCloudVenue == null || cloudVenue.lastUpdated > storedCloudVenue.lastUpdated) {
                    postCloudVenue(cloudVenue)
                }
            }.addOnFailureListener{
                Timber.w("Error getting firebase data")
            }
        }
    }

    private fun postCloudVenue(cloudVenue: CloudVenue) {
        database.reference.child("users").child(userId).child("venues").child(cloudVenue.venueId).setValue(cloudVenue)
    }

    fun clearSubscriptions() {
        listener?.let { database.reference.removeEventListener(listener!!)}
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

data class CloudVenue(val venueId: String = "", val visited: Boolean = false, val hidden: Boolean = false, val lastUpdated: Int = 0) {

}

fun List<CloudVenue>.asDomainModel(): List<Venue> {
    return map {
        Venue(
            id = it.venueId,
            name = "",
            lat = 0.0,
            lng = 0.0,
            reviews = 0,
            rating = 0.0,
            imageUrl = "",
            url = "",
            distance = 0.0,
            visited = it.visited,
            hidden = it.hidden,
            lastUserUpdate = Date(it.lastUpdated.toLong() * 1000)
        )
    }
}
fun List<Venue>.asCloudModel(): List<CloudVenue> {
    return map {
        CloudVenue(
            venueId = it.id,
            visited = it.visited,
            hidden = it.hidden,
            lastUpdated = it.lastUserUpdate.toInstant().epochSecond.toInt()
        )
    }
}

