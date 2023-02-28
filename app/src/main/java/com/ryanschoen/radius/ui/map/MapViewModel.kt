package com.ryanschoen.radius.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import com.ryanschoen.radius.domain.Venue
import com.ryanschoen.radius.repository.getRepository
import timber.log.Timber

public class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = getRepository(application)

    private var _navigateToSetup = MutableLiveData<Boolean>()
    val navigateToSetup: LiveData<Boolean>
        get() = _navigateToSetup

    val venues = repo.venues

    init {
        if (repo.isAddressReady()) {
            Timber.i("Found saved address: " + repo.getSavedAddress())
        }
        else {
            Timber.i("No saved address :(")
            _navigateToSetup.value = true
        }
    }

    fun onNavigateToSetupDone() {
        _navigateToSetup.value = false
    }

    fun getHomeLat(): Double {
        return repo.getSavedLatitude()
    }
    fun getHomeLng(): Double {
        return repo.getSavedLongitude()
    }

}