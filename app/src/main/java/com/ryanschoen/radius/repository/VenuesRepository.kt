package com.ryanschoen.radius.repository

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.ryanschoen.radius.R
import com.ryanschoen.radius.database.DatabaseVenue
import com.ryanschoen.radius.database.asDomainModel
import com.ryanschoen.radius.database.getCloudDatabase
import com.ryanschoen.radius.database.getDatabase
import com.ryanschoen.radius.domain.Venue
import com.ryanschoen.radius.network.asDatabaseModel
import com.ryanschoen.radius.network.fetchVenues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDateTime

class VenuesRepository(application: Application) {
    private val database = getDatabase(application)
    private val cloudDatabase = getCloudDatabase(application)
    private val sharedPref = application.getSharedPreferences(
        application.getString(R.string.preference_file_key),
        Context.MODE_PRIVATE
    )

    init {
        cloudDatabase.setUserId(userFirebaseId)
    }

    companion object {
        const val SAVED_ADDRESS_STRING = "saved_address"
        const val SAVED_LATITUDE_STRING = "saved_latitude"
        const val SAVED_LONGITUDE_STRING = "saved_longitude"
        const val SAVED_ADDRESS_READY = "saved_address_ready"
        const val YELP_DATA_READY = "yelp_data_read"
        const val YELP_DATA_EXPIRATION = "yelp_data_expiration"
        const val SAVED_USER_EMAIL = "saved_user_email"
        const val SAVED_USER_FIREBASE_ID = "saved_user_firebase_id"

        const val YELP_DATA_EXPIRATION_HOURS = 24
    }


    val venues: LiveData<List<Venue>> = database.venueDao.getActiveVenues().map {
        it.asDomainModel()
    }

    val visibleVenues: LiveData<List<Venue>> = database.venueDao.getVisibleActiveVenues().map {
        it.asDomainModel()
    }

    fun getNthVenue(n: Int): LiveData<Double> = database.venueDao.getNthVenueDistance(n)
    suspend fun clearYelpData() = withContext(Dispatchers.IO) {
        database.venueDao.clearYelpData()
    }


    fun getSavedAddress(): String? {
        return sharedPref.getString(SAVED_ADDRESS_STRING, "")
    }

    fun getSavedLatitude(): Double {
        return sharedPref.getFloat(SAVED_LATITUDE_STRING, 0.0f).toDouble()
    }

    fun getSavedLongitude(): Double {
        return sharedPref.getFloat(SAVED_LONGITUDE_STRING, 0.0f).toDouble()
    }

    fun isAddressReady(): Boolean {
        return sharedPref.getBoolean(SAVED_ADDRESS_READY, false)
    }

    private fun setAddressIsReady() {
        sharedPref.edit().putBoolean(SAVED_ADDRESS_READY, true).apply()
    }

    var yelpDataReady: Boolean
        get() = sharedPref.getBoolean(YELP_DATA_READY, false)
        set(ready) = sharedPref.edit().putBoolean(YELP_DATA_READY, ready).apply()

    var userEmail: String
        get() = sharedPref.getString(SAVED_USER_EMAIL, "") ?: ""
        set(email) = sharedPref.edit().putString(SAVED_USER_EMAIL, email).apply()


    var userFirebaseId: String
        get() = sharedPref.getString(SAVED_USER_FIREBASE_ID, "") ?: ""
        set(email) = sharedPref.edit().putString(SAVED_USER_FIREBASE_ID, email).apply()

    private val yelpDataHasExpired: Boolean
        get() = LocalDateTime.now().isAfter(yelpDataExpiration)
    val shouldRefreshYelpData: Boolean
        get() = !yelpDataReady || yelpDataHasExpired


    private fun refreshYelpExpiration(hours: Int = YELP_DATA_EXPIRATION_HOURS) {
        val yelpDataExpiration = LocalDateTime.now().plusHours(hours.toLong())
        sharedPref.edit().putString(YELP_DATA_EXPIRATION, yelpDataExpiration.toString()).apply()
    }

    private val yelpDataExpiration: LocalDateTime
        get() = LocalDateTime.parse(
            sharedPref.getString(
                YELP_DATA_EXPIRATION,
                "2018-12-30T19:34:50.63"
            )
        )

    fun setSavedAddressLatLong(address: String, lat: Double, lng: Double) {
        with(sharedPref.edit()) {
            putString(SAVED_ADDRESS_STRING, address)
            putFloat(SAVED_LATITUDE_STRING, lat.toFloat())
            putFloat(SAVED_LONGITUDE_STRING, lng.toFloat())
            apply()
        }
        // TODO: update database
    }

    private fun clearSharedPrefs() {
        sharedPref.edit().clear().apply()
    }

    suspend fun downloadVenues(address: String): Int = withContext(Dispatchers.IO) {
        yelpDataReady = false
        deactivateAllVenues()

        val venues = fetchVenues(address)
        val dbVenues = venues.asDatabaseModel()

        var maxDistance: Double = -1.0
        for (venue in dbVenues) {
            if (venue.lat == null || venue.lng == null) {
                continue
            }
            upsertVenue(venue)
            if (venue.distance > maxDistance) {
                maxDistance = venue.distance
            }
        }
        database.venueDao.activateVenuesInRange(maxDistance)
        refreshYelpExpiration()
        yelpDataReady = true
        Timber.d("Inserted ${venues.businesses.size} venues")
        setAddressIsReady()
        venues.businesses.size
    }

    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        database.venueDao.deleteVenuesData()
        clearSharedPrefs()
    }

    suspend fun setVenueState(venueId: String, visited: Boolean, hidden: Boolean) = withContext(Dispatchers.IO) {
        database.venueDao.setVenueState(venueId, visited, hidden)
        cloudDatabase.setVenueState(venueId, visited, hidden)
    }

    private suspend fun deactivateAllVenues() = withContext(Dispatchers.IO) {
        database.venueDao.deactivateAllVenues()
    }

    private fun upsertVenue(item: DatabaseVenue) {
        try {
            database.venueDao.insertVenue(item)
        } catch (exception: SQLiteConstraintException) {
            val oldItem = database.venueDao.getVenueById(item.id)
            database.venueDao.updateVenue(item.apply {
                active = oldItem.active
                visited = oldItem.visited
                hidden = oldItem.hidden
                // you can add more fields here
            })
        } catch (throwable: Throwable) {
            val oldItem = database.venueDao.getVenueById(item.id)
            database.venueDao.updateVenue(item.apply {
                active = oldItem.active
                visited = oldItem.visited
                hidden = oldItem.hidden
                // you can add more fields here
            })
        }
    }

    fun setUserData(email: String?, uid: String) {
        userEmail = email ?: ""
        userFirebaseId = uid
        cloudDatabase.setUserId(userFirebaseId)
    }
    val userIsSignedIn: Boolean
        get() = userEmail.isNotEmpty()

}

private lateinit var INSTANCE: VenuesRepository

fun getRepository(application: Application): VenuesRepository {
    synchronized(VenuesRepository::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = VenuesRepository(application)
        }
    }
    return INSTANCE
}



