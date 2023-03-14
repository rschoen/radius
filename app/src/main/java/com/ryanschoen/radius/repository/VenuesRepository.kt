package com.ryanschoen.radius.repository

import android.app.Application
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.ryanschoen.radius.R
import com.ryanschoen.radius.database.DatabaseVenue
import com.ryanschoen.radius.database.asDomainModel
import com.ryanschoen.radius.database.getDatabase
import com.ryanschoen.radius.domain.Venue
import com.ryanschoen.radius.network.asDatabaseModel
import com.ryanschoen.radius.network.fetchVenues
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class VenuesRepository(application: Application) {
    private val database = getDatabase(application)
    private val sharedPref = application.getSharedPreferences(
    application.getString(R.string.preference_file_key),
    Context.MODE_PRIVATE)

    companion object {
        const val SAVED_ADDRESS_STRING = "saved_address"
        const val SAVED_LATITUDE_STRING = "saved_latitude"
        const val SAVED_LONGITUDE_STRING = "saved_longitude"
        const val SAVED_ADDRESS_READY = "saved_address_ready"
    }


    val venues: LiveData<List<Venue>> = database.venueDao.getVenues().map {
        it.asDomainModel()
    }

    fun getNthVenue(n: Int): LiveData<Double> = database.venueDao.getNthVenueDistance(n)
    val venuesRadius: LiveData<Double> = database.venueDao.getMaximumVenueDistance()
    val visitedRadius: LiveData<Double> = database.venueDao.getMaximumAllVisitedDistance()


    fun getSavedAddress(): String? {
        return sharedPref.getString(SAVED_ADDRESS_STRING,"")
    }
    fun getSavedLatitude(): Double {
        return sharedPref.getFloat(SAVED_LATITUDE_STRING,0.0f).toDouble()
    }
    fun getSavedLongitude(): Double {
        return sharedPref.getFloat(SAVED_LONGITUDE_STRING,0.0f).toDouble()
    }
    fun isAddressReady(): Boolean {
        return sharedPref.getBoolean(SAVED_ADDRESS_READY,false)
    }
    fun setAddressReady(ready: Boolean) {
        sharedPref.edit().putBoolean(SAVED_ADDRESS_READY,ready).apply()
    }

    fun setSavedAddressLatLong(address: String, lat: Double, lng: Double) {
        with (sharedPref.edit()) {
            putString(SAVED_ADDRESS_STRING, address)
            putFloat(SAVED_LATITUDE_STRING, lat.toFloat())
            putFloat(SAVED_LONGITUDE_STRING, lng.toFloat())
            apply()
        }
    }

    fun clearSharedPrefs() {
        sharedPref.edit().clear().apply()
    }

    suspend fun downloadVenues(address: String): Int = withContext(Dispatchers.IO) {
        val venues = fetchVenues(address)
        val dbVenues = venues.asDatabaseModel()

        var maxDistance: Double = -1.0
        for(venue in dbVenues) {
            upsertVenue(venue)
            if(venue.distance > maxDistance) {
                maxDistance = venue.distance
            }
        }
        database.venueDao.activateVenuesInRange(maxDistance)

        Timber.i("Inserted ${venues.businesses.size} venues")
        setAddressReady(true)
        venues.businesses.size
    }

    suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            database.venueDao.deleteVenuesData()
            clearSharedPrefs()
        }
    }
    suspend fun setVenueVisited(venueId: String, visited: Boolean) {
        withContext(Dispatchers.IO) {

            Timber.i("Storing async into the database: ${venueId}.visited = ${visited}")
            database.venueDao.setVenueVisited(venueId, visited)
        }
    }

    suspend fun deactivateAllVenues() {
        withContext(Dispatchers.IO) {
            database.venueDao.deactivateAllVenues()
        }
    }

     private fun upsertVenue(item: DatabaseVenue) {
        try{
            database.venueDao.insertVenue(item)
        }catch (exception: SQLiteConstraintException){
            val oldItem = database.venueDao.getVenueById(item.id)
            database.venueDao.updateVenue(item.apply {
                active = oldItem.active
                visited = oldItem.visited
                hidden = oldItem.hidden
                // you can add more fields here
            })
        }catch (throwable: Throwable){
            val oldItem = database.venueDao.getVenueById(item.id)
            database.venueDao.updateVenue(item.apply {
                active = oldItem.active
                visited = oldItem.visited
                hidden = oldItem.hidden
                // you can add more fields here
            })
        }
    }
}

private lateinit var INSTANCE: VenuesRepository

fun getRepository(application: Application): VenuesRepository {
    synchronized(VenuesRepository::class.java) {
        if(!::INSTANCE.isInitialized) {
            INSTANCE = VenuesRepository(application)
        }
    }
    return INSTANCE
}



