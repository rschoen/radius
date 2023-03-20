package com.ryanschoen.radius.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ryanschoen.radius.repository.getRepository
import kotlinx.coroutines.launch

open class RadiusViewModel(application: Application) : AndroidViewModel(application) {
    internal val repo = getRepository(application)

    internal var _navigateToSetup = MutableLiveData<Boolean>()
    val navigateToSetup: LiveData<Boolean>
        get() = _navigateToSetup


    internal var _quitActivity = MutableLiveData<Boolean>()
    val quitActivity: LiveData<Boolean>
        get() = _quitActivity

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
        }
    }

    fun setVenueVisited(venueId: String, visited: Boolean) {
        viewModelScope.launch {
            repo.setVenueVisited(venueId, visited)
        }
    }
}