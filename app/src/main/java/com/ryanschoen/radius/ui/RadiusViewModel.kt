package com.ryanschoen.radius.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ryanschoen.radius.repository.getRepository
import kotlinx.coroutines.launch
import timber.log.Timber

open class RadiusViewModel(application: Application) : AndroidViewModel(application) {
    internal val repo = getRepository(application)

    internal var _navigateToSetup = MutableLiveData<Boolean>()
    val navigateToSetup: LiveData<Boolean>
        get() = _navigateToSetup


    private var _quitActivity = MutableLiveData<Boolean>()
    val quitActivity: LiveData<Boolean>
        get() = _quitActivity

    private var _startedDownloadingVenues = MutableLiveData<Boolean>()
    val startedDownloadingVenues: LiveData<Boolean>
        get() = _startedDownloadingVenues


    private var _doneDownloadingVenues = MutableLiveData<Boolean>()
    val doneDownloadingVenues: LiveData<Boolean>
        get() = _doneDownloadingVenues

    init {
        if(!repo.isAddressReady()) {
            Timber.i("No saved address found, redirecting to setup")
            _navigateToSetup.value = true
        } else if(repo.shouldRefreshYelpData) {
            viewModelScope.launch {
                _startedDownloadingVenues.value = true
                repo.downloadVenues(repo.getSavedAddress()!!)
                _doneDownloadingVenues.value = true
            }
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repo.deleteAllData()
            _quitActivity.value = true
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

    fun clearYelpData() {
        viewModelScope.launch {
            repo.clearYelpData()
            repo.yelpDataReady = false
        }
    }

    fun setVenueVisited(venueId: String, visited: Boolean) {
        viewModelScope.launch {
            repo.setVenueVisited(venueId, visited)
        }
    }


    fun onStartedDownloadingVenues() {
        _startedDownloadingVenues.value = false
    }
    fun onDoneDownloadingVenues() {
        _doneDownloadingVenues.value = false
    }
}