package com.ryanschoen.radius.ui.settings

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ryanschoen.radius.ui.RadiusViewModel
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : RadiusViewModel(application) {

    fun clearAllData() {
        viewModelScope.launch {
            repo.deleteAllData()
            _quitActivity.value = true
        }
    }

    fun clearNetworkData() {
        viewModelScope.launch {
            repo.clearDownloadedNetworkData()
            repo.networkVenueDataReady = false
        }
    }

    private var _quitActivity = MutableLiveData<Boolean>()
    val quitActivity: LiveData<Boolean>
        get() = _quitActivity


    var address = repo.getSavedAddress()
    val userEmail: String
        get() = repo.userEmail
    val userIsSignedIn: Boolean
        get() = repo.userIsSignedIn


}