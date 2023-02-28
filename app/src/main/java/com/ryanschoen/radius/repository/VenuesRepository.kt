package com.ryanschoen.radius.repository

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.ryanschoen.radius.R
import com.ryanschoen.radius.database.asDomainModel
import com.ryanschoen.radius.database.getDatabase
import com.ryanschoen.radius.domain.Venue

class VenuesRepository(application: Application) {
    val database = getDatabase(application)
    val sharedPref = application.getSharedPreferences(
    application.getString(R.string.preference_file_key),
    Context.MODE_PRIVATE)
    var savedAddressString = application.getString(R.string.saved_address)
    val venues: LiveData<List<Venue>> = Transformations.map(database.venueDao.getVenues()) {
        it.asDomainModel()
    }


    fun getSavedAddress(): String? {
        return sharedPref.getString(savedAddressString,null)
    }

    fun hasSavedAddress(): Boolean {
        return !getSavedAddress().isNullOrBlank()
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