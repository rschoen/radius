package com.ryanschoen.radius.ui.venues

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VenuesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Someday, this will be a list of venues!"
    }
    val text: LiveData<String> = _text
}