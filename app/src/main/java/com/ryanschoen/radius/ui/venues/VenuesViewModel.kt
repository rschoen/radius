package com.ryanschoen.radius.ui.venues

import android.app.Application
import com.ryanschoen.radius.ui.RadiusViewModel

class VenuesViewModel(application: Application) : RadiusViewModel(application) {

    val venues = repo.venues

    init {
        if (!repo.isAddressReady()) {
            _navigateToSetup.value = true
        }
    }

}