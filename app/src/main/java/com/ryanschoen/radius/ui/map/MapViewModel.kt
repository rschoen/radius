package com.ryanschoen.radius.ui.map

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ryanschoen.radius.ui.RadiusViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class MapViewModel(application: Application) : RadiusViewModel(application) {


    private var _doneDownloadingVenues = MutableLiveData<Boolean>()
    val doneDownloadingVenues: LiveData<Boolean>
        get() = _doneDownloadingVenues

    var venues = repo.visibleVenues
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




    fun onDoneDownloadingVenues() {
        _doneDownloadingVenues.value = false
    }

}