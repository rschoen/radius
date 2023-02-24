package com.ryanschoen.radius.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng


object AddressManager {
    private val _address = MutableLiveData<String>()
    val address: LiveData<String>
        get() = _address

    private val _latlng = MutableLiveData<LatLng>()
    val latlng: LiveData<LatLng>
        get() = _latlng

    private val _long = MutableLiveData<Float>()
    val long: LiveData<Float>
        get() = _long

    fun loadValidatedAddress(addr: String, lat: Double, long: Double) {
        _address.value = addr
        _latlng.value = LatLng(lat,long)
    }

    fun validateAndSaveAddress(addr: String, callback: (wasSuccessful: Boolean, validatedAddress: String, lat: Double, long: Double) -> Unit) {

        //TODO: validate address
        //TODO: parse response and trigger callback

        callback(true,addr,1.0,-1.0)
    }



}