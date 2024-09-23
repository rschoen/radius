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
import com.ryanschoen.radius.metersBetweenPoints
import com.ryanschoen.radius.network.asDatabaseModel
import com.ryanschoen.radius.network.fetchVenues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDateTime
import java.util.Date

class VenuesRepository(application: Application) {
    private val database = getDatabase(application)
    private val cloudDatabase = getCloudDatabase()
    private val sharedPref = application.getSharedPreferences(
        application.getString(R.string.preference_file_key),
        Context.MODE_PRIVATE
    )

    init {
        cloudDatabase.setUserId(userFirebaseId)
        if(userIsSignedIn) {
            syncVenuesAndSubscribe()
        }
    }

    companion object {
        const val SAVED_ADDRESS_STRING = "saved_address"
        const val SAVED_LATITUDE_STRING = "saved_latitude"
        const val SAVED_LONGITUDE_STRING = "saved_longitude"
        const val SAVED_ADDRESS_READY = "saved_address_ready"
        const val NETWORK_DATA_READY = "network_data_ready"
        const val NETWORK_DATA_EXPIRATION = "network_data_expiration"
        const val SAVED_USER_EMAIL = "saved_user_email"
        const val SAVED_USER_FIREBASE_ID = "saved_user_firebase_id"

        const val NETWORK_DATA_EXPIRATION_HOURS = 1
    }


    val venues: LiveData<List<Venue>> = database.venueDao.getActiveVenues().map {
        it.asDomainModel()
    }

    val visibleVenues: LiveData<List<Venue>> = database.venueDao.getVisibleActiveVenues().map {
        it.asDomainModel()
    }

    fun getNthVenue(n: Int): LiveData<Double> = database.venueDao.getNthVenueDistance(n)
    suspend fun clearDownloadedNetworkData() = withContext(Dispatchers.IO) {
        database.venueDao.clearDownloadedNetworkData()
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

    var networkVenueDataReady: Boolean
        get() = sharedPref.getBoolean(NETWORK_DATA_READY, false)
        set(ready) = sharedPref.edit().putBoolean(NETWORK_DATA_READY, ready).apply()

    var userEmail: String
        get() = sharedPref.getString(SAVED_USER_EMAIL, "") ?: ""
        set(email) = sharedPref.edit().putString(SAVED_USER_EMAIL, email).apply()


    var userFirebaseId: String
        get() = sharedPref.getString(SAVED_USER_FIREBASE_ID, "") ?: ""
        set(email) = sharedPref.edit().putString(SAVED_USER_FIREBASE_ID, email).apply()

    val shouldRefreshNetworkData: Boolean
        get() = LocalDateTime.now().isAfter(networkDataExpiration)


    private fun refreshNetworkDataExpiration(hours: Int = NETWORK_DATA_EXPIRATION_HOURS) {
        val networkDataExpiration = LocalDateTime.now().plusHours(hours.toLong())
        sharedPref.edit().putString(NETWORK_DATA_EXPIRATION, networkDataExpiration.toString()).apply()
    }

    private val networkDataExpiration: LocalDateTime
        get() = LocalDateTime.parse(
            sharedPref.getString(
                NETWORK_DATA_EXPIRATION,
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

    suspend fun downloadVenues(lat: Double, lng: Double): Int = withContext(Dispatchers.IO) {
        networkVenueDataReady = false
        deactivateAllVenues()

        val result = fetchVenues(lat, lng)
        val dbVenues = result.asDatabaseModel()

        var maxDistance: Double = -1.0
        for (venue in dbVenues) {
            if (venue.lat == null || venue.lng == null) {
                continue
            }
            venue.distance = metersBetweenPoints(lat, lng, venue.lat, venue.lng)
            Timber.i("Calculated distance as ${venue.distance}")
            upsertVenue(venue)
            if (venue.distance > maxDistance) {
                maxDistance = venue.distance
                Timber.i("Max distance now $maxDistance")
            }
        }
        database.venueDao.activateVenuesInRange(maxDistance)
        refreshNetworkDataExpiration()
        networkVenueDataReady = true
        Timber.d("Inserted ${result.venues.size} venues")
        setAddressIsReady()
        result.venues.size
    }

    suspend fun deleteAllData() = withContext(Dispatchers.IO) {
        database.venueDao.deleteVenuesData()
        clearSharedPrefs()
    }

    suspend fun setVenueState(venueId: String, visited: Boolean, hidden: Boolean) = withContext(Dispatchers.IO) {
        database.venueDao.setVenueState(venueId, visited, hidden)
        cloudDatabase.setVenueState(venueId, visited, hidden, Date())
    }

    private fun setLocalVenueStateWithTimestamp(venueId: String, visited: Boolean, hidden: Boolean, timestamp: Date) {
        database.venueDao.setVenueStateWithoutTimestamp(venueId, visited, hidden, timestamp.toInstant().epochSecond.toInt())
    }
    private fun setCloudVenueState(venueId: String, visited: Boolean, hidden: Boolean, timestamp: Date) {
        cloudDatabase.setVenueState(venueId, visited, hidden, timestamp)
    }
    private suspend fun deactivateAllVenues() = withContext(Dispatchers.IO) {
        database.venueDao.deactivateAllVenues()
    }

    private fun syncVenuesAndSubscribe() {
        this@VenuesRepository.cloudDatabase.subscribeToVenueChanges { venuesFromCloud: List<Venue> ->
            val venuesSnapshot = venues.value
            venuesSnapshot?.let {
                for (venueFromCloud in venuesFromCloud) {
                    for (venue in venuesSnapshot) {
                        if (venue.id == venueFromCloud.id) {
                            if(venueFromCloud.lastUserUpdate > venue.lastUserUpdate) {
                                if (venueFromCloud.visited != venue.visited || venueFromCloud.hidden != venue.hidden) {
                                    setLocalVenueStateWithTimestamp(
                                        venueFromCloud.id,
                                        venueFromCloud.visited,
                                        venueFromCloud.hidden,
                                        venueFromCloud.lastUserUpdate
                                    )
                                }
                            } else if (venueFromCloud.lastUserUpdate < venue.lastUserUpdate) {
                                setCloudVenueState(
                                    venue.id,
                                    venue.visited,
                                    venue.hidden,
                                    venue.lastUserUpdate
                                )
                            }

                            break
                        }
                    }
                }

            }
        }
    }

    fun initialSync() {
        this@VenuesRepository.cloudDatabase.getOneTimeSnapshot { venuesFromCloud: List<Venue> ->
            val venuesSnapshot = venues.value
            venuesSnapshot?.let {
                for (venue in venuesSnapshot) {
                    var found = false
                    for (venueFromCloud in venuesFromCloud) {
                        if (venue.id == venueFromCloud.id) {
                            if(venueFromCloud.lastUserUpdate > venue.lastUserUpdate) {
                                setLocalVenueStateWithTimestamp(
                                    venueFromCloud.id,
                                    venueFromCloud.visited,
                                    venueFromCloud.hidden,
                                    venueFromCloud.lastUserUpdate
                                )
                            }
                        } else if (venueFromCloud.lastUserUpdate < venue.lastUserUpdate) {
                            setCloudVenueState(
                                venue.id,
                                venue.visited,
                                venue.hidden,
                                venue.lastUserUpdate
                            )
                        }
                        found = true
                        break
                    }
                    if(!found) {
                        setCloudVenueState(
                            venue.id,
                            venue.visited,
                            venue.hidden,
                            venue.lastUserUpdate
                        )
                    }
                }

            }
        }
    }

    fun unsubscribeFromCloudUpdates() {
        cloudDatabase.clearSubscriptions()
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
            })
        } catch (throwable: Throwable) {
            val oldItem = database.venueDao.getVenueById(item.id)
            database.venueDao.updateVenue(item.apply {
                active = oldItem.active
                visited = oldItem.visited
                hidden = oldItem.hidden
            })
        }
    }

    suspend fun setUserData(email: String?, uid: String) {
        userEmail = email ?: ""
        userFirebaseId = uid
        cloudDatabase.setUserId(userFirebaseId)

        if(userIsSignedIn) {
            withContext(Dispatchers.IO) {
                syncVenuesAndSubscribe()
            }
        } else {
            unsubscribeFromCloudUpdates()
        }
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



