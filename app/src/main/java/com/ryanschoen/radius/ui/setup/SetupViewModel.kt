package com.ryanschoen.radius.ui.setup

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.ryanschoen.radius.network.*
import com.ryanschoen.radius.repository.getRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class SetupViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = getRepository(application)

    init {
        if (repo.hasSavedAddress()) {
            Timber.i("Found saved address: " + repo.getSavedAddress())
        }
        else {
            Timber.i("No saved address :(")
        }
    }

    private var _verifiedAddress = MutableLiveData<String>()
    val verifiedAddress: LiveData<String>
        get() = _verifiedAddress

    private var _latLng = MutableLiveData<LatLng>()
    val latLng: LiveData<LatLng>
        get() = _latLng

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
            val addressResult = sendAddressVerification(
                NetworkAddress(
                    listOf<String>(address),
                    locality,
                    administrativeArea,
                    regionCode
                )
            )
            if(addressResult == null) {
                //TODO: do something more meaningful here
                Toast.makeText(getApplication(),"Address verification failed due to an internal error. Please retry!",Toast.LENGTH_LONG).show()
            } else if (!addressResult.complete) {
                _latLng.value = LatLng(0.0,0.0)
                _verifiedAddress.value = ""
                _addressChanged.value = true
            } else {

                _latLng.value = LatLng(addressResult.latitude, addressResult.longitude)
                _verifiedAddress.value = addressResult.formattedAddress
                _addressChanged.value = true

                val venues = fetchVenues(addressResult.formattedAddress)
                _numVenues.value = venues.size
                _venuesChanged.value = true
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