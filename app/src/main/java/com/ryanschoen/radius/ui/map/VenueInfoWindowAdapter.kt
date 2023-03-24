package com.ryanschoen.radius.ui.map

import android.view.View
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import com.ryanschoen.radius.databinding.VenueInfoWindowBinding
import com.ryanschoen.radius.domain.Venue

class VenueInfoWindowAdapter(private val binding: VenueInfoWindowBinding, private val mapWrapperLayout: MapWrapperLayout) : InfoWindowAdapter {

    override fun getInfoContents(marker: Marker): View? {
        marker.tag?.let {
            val venue = marker.tag as Venue
            binding.venue = venue
            binding.executePendingBindings()
            mapWrapperLayout.setMarkerWithInfoWindow(marker, binding.root)
            return binding.root
        }
        return null
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }
}