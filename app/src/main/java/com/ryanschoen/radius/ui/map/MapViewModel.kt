package com.ryanschoen.radius.ui.map

import android.app.Application
import com.ryanschoen.radius.ui.RadiusViewModel

class MapViewModel(application: Application) : RadiusViewModel(application) {

    var venues = repo.visibleVenues
    val tenthVenueDistance = repo.getNthVenue(10)
    var addressIsReady = repo.isAddressReady()

}