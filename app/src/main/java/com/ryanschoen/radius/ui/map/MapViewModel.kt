package com.ryanschoen.radius.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ryanschoen.radius.repository.getRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = getRepository(application)

    private var _navigateToSetup = MutableLiveData<Boolean>()
    val navigateToSetup: LiveData<Boolean>
        get() = _navigateToSetup

    private var _quitActivity = MutableLiveData<Boolean>()
    val quitActivity: LiveData<Boolean>
        get() = _quitActivity

    private var _doneDownloadingVenues = MutableLiveData<Boolean>()
    val doneDownloadingVenues: LiveData<Boolean>
        get() = _doneDownloadingVenues

    val venues = repo.visibleVenues
    val tenthVenueDistance = repo.getNthVenue(10)
    var addressIsReady = repo.isAddressReady()

    init {
        if(!repo.isAddressReady()) {
            Timber.i("No saved address found, redirecting to setup")
            _navigateToSetup.value = true
        } else if(repo.shouldRefreshYelpData) {
            viewModelScope.launch {
                repo.downloadVenues(repo.getSavedAddress()!!)

                // TODO: show a loading bar!
                _doneDownloadingVenues.value = true
            }
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

    fun clearYelpData() {
        viewModelScope.launch {
            repo.clearYelpData()
        }
    }

    fun setVenueVisited(venueId: String, visited: Boolean) {
        viewModelScope.launch {
            repo.setVenueVisited(venueId, visited)
        }
    }

    fun onDoneDownloadingVenues() {
        _doneDownloadingVenues.value = false
    }

}