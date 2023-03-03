package com.ryanschoen.radius.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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

    private var _verifiedAddress = MutableLiveData<String>()
    val verifiedAddress: LiveData<String>
        get() = _verifiedAddress

    private var _addressChanged = MutableLiveData<Boolean>()
    val addressChanged: LiveData<Boolean>
        get() = _addressChanged
    private var _numVenues = MutableLiveData<Int>()
    val numVenues: LiveData<Int>
        get() = _numVenues

    private var _venuesChanged = MutableLiveData<Boolean>()
    val venuesChanged: LiveData<Boolean>
        get() = _venuesChanged

    fun verifyAddress(address: String, locality: String, administrativeArea: String, regionCode: String = "US") {

        viewModelScope.launch {
            val addressVerified = repo.verifyAndStoreAddress(address, locality, administrativeArea, regionCode)
            if (addressVerified) {
                _verifiedAddress.value = repo.getSavedAddress()
                _addressChanged.value = true

                val venuesDownloaded = repo.downloadVenues(_verifiedAddress.value!!)
                _numVenues.value = venuesDownloaded
                _venuesChanged.value = true
            }
            else {
                _verifiedAddress.value = ""
                _addressChanged.value = true
            }
        }

    }

    fun onAddressChangedComplete() {
        _addressChanged.value = false
    }
    fun onVenuesChangedComplete() {
        _venuesChanged.value = false
    }
}