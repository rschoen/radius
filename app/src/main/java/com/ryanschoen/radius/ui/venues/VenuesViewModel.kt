package com.ryanschoen.radius.ui.venues

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.ryanschoen.radius.ui.RadiusViewModel
import kotlinx.coroutines.launch

class VenuesViewModel(application: Application) : RadiusViewModel(application) {

    val venues = repo.venues

    init {
        if (!repo.isAddressReady()) {
            _navigateToSetup.value = true
        }
    }

    fun toggleVenueIsHidden(id: String) = viewModelScope.launch {
        repo.toggleVenueIsHidden(id)
    }

}