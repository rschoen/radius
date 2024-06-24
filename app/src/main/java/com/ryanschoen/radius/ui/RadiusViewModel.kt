package com.ryanschoen.radius.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.ryanschoen.radius.repository.getRepository
import kotlinx.coroutines.launch

open class RadiusViewModel(application: Application) : AndroidViewModel(application) {
    internal val repo = getRepository(application)

    internal var _navigateToSetup = MutableLiveData<Boolean>()
    val navigateToSetup: LiveData<Boolean>
        get() = _navigateToSetup


    private var _startedDownloadingVenues = MutableLiveData<Boolean>()
    val startedDownloadingVenues: LiveData<Boolean>
        get() = _startedDownloadingVenues


    private var _doneDownloadingVenues = MutableLiveData<Boolean>()
    val doneDownloadingVenues: LiveData<Boolean>
        get() = _doneDownloadingVenues

    init {
        if (!repo.isAddressReady()) {
            _navigateToSetup.value = true
        } else if (repo.shouldRefreshYelpData) {
            viewModelScope.launch {
                _startedDownloadingVenues.value = true
                repo.downloadVenues(repo.getSavedAddress()!!)
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


    fun setVenueState(venueId: String, visited: Boolean, hidden: Boolean) {
        viewModelScope.launch {
            repo.setVenueState(venueId, visited, hidden)
        }
    }


    fun onStartedDownloadingVenues() {
        _startedDownloadingVenues.value = false
    }

    fun onDoneDownloadingVenues() {
        _doneDownloadingVenues.value = false
    }

    fun setCurrentUser(user: FirebaseUser) {
        repo.setUserData(user.email, user.uid)
    }

    fun clearCurrentUser() {
        repo.setUserData("", "")
    }
}