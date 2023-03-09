package com.ryanschoen.radius.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.ryanschoen.radius.R
import com.ryanschoen.radius.database.asDomainModel
import com.ryanschoen.radius.database.getDatabase
import com.ryanschoen.radius.domain.Venue
import com.ryanschoen.radius.network.NetworkAddress
import com.ryanschoen.radius.network.asDatabaseModel
import com.ryanschoen.radius.network.fetchVenues
import com.ryanschoen.radius.network.sendAddressVerification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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


    val venues: LiveData<List<Venue>> = Transformations.map(database.venueDao.getVenues()) {
        Timber.i(database.venueDao.getVenues().toString())
        it.asDomainModel()
    }

    val tenthVenueDistance: LiveData<Double> = database.venueDao.getTenthVenue()


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



    suspend fun verifyAndStoreAddress(address: String, locality: String, administrativeArea: String, regionCode: String): Boolean {

            val addressResult =
            withContext(Dispatchers.IO) {
                sendAddressVerification(
                    NetworkAddress(
                        listOf<String>(address),
                        locality,
                        administrativeArea,
                        regionCode
                    )
                )

            }
            if (addressResult == null) {
                //TODO: do something more meaningful here
                return false
            } else if (!addressResult.complete) {
                return false
            } else {
                setSavedAddressLatLong(
                    addressResult.formattedAddress,
                    addressResult.latitude,
                    addressResult.longitude
                )
                return true
            }
    }

    suspend fun downloadVenues(address: String): Int {
        val venues =
            withContext(Dispatchers.IO) {
                val venues = fetchVenues(address)
                database.venueDao.insertAll(*venues.asDatabaseModel())
                Timber.i("Inserted ${venues.businesses.size} venues")
                venues
            }
        setAddressReady(true)
        return venues.businesses.size
    }

    suspend fun deleteAllData() {
        withContext(Dispatchers.IO) {
            database.venueDao.deleteVenuesData()
            clearSharedPrefs()
        }
    }
    suspend fun setVenueVisited(venueId: String, visited: Boolean) {
        withContext(Dispatchers.IO) {

            Timber.i("Into the database: ${venueId}.visited = ${visited}")
            database.venueDao.setVenueVisited(venueId, visited)
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



