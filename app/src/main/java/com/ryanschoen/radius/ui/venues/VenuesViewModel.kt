package com.ryanschoen.radius.ui.venues

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ryanschoen.radius.repository.getRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class VenuesViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = getRepository(application)

    private var _navigateToSetup = MutableLiveData<Boolean>()
    val navigateToSetup: LiveData<Boolean>
        get() = _navigateToSetup

    private var _quitActivity = MutableLiveData<Boolean>()
    val quitActivity: LiveData<Boolean>
        get() = _quitActivity

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

    fun clearAllData() {
        viewModelScope.launch {
            repo.deleteAllData()
            _quitActivity.value = true
        }
    }

    fun setVenueVisited(venueId: String, visited: Boolean) {
        viewModelScope.launch {
            repo.setVenueVisited(venueId, visited)
        }
    }

}