package com.ryanschoen.radius.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.ryanschoen.radius.repository.getRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class SetupViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = getRepository(application)

    init {
        if (repo.isAddressReady()) {
            Timber.i("Found saved address: " + repo.getSavedAddress())
        }
        else {
            Timber.i("No saved address :(")
        }
    }

    private var _numVenues = MutableLiveData<Int>()
    val numVenues: LiveData<Int>
        get() = _numVenues

    private var _venuesChanged = MutableLiveData<Boolean>()
    val venuesChanged: LiveData<Boolean>
        get() = _venuesChanged

    fun loadVenues(address: String, latlng: LatLng) {

        viewModelScope.launch {
            repo.setSavedAddressLatLong(address, latlng.latitude, latlng.longitude)
            val venuesDownloaded = repo.downloadVenues(address)
            _numVenues.value = venuesDownloaded
            _venuesChanged.value = true

        }

    }

    fun onVenuesChangedComplete() {
        _venuesChanged.value = false
    }
}