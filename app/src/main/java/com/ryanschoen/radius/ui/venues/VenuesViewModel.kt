package com.ryanschoen.radius.ui.venues

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.ryanschoen.radius.ui.RadiusViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class VenuesViewModel(application: Application) : RadiusViewModel(application) {


    val venues = repo.venues

    init {
        if (repo.isAddressReady()) {
            Timber.i("Found saved address: " + repo.getSavedAddress())
        } else {
            Timber.i("No saved address :(")
            _navigateToSetup.value = true
        }
    }


    fun toggleVenueIsHidden(id: String) = viewModelScope.launch {
        repo.toggleVenueIsHidden(id)
    }

}