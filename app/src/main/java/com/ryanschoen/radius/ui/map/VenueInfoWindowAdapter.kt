package com.ryanschoen.radius.ui.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import com.ryanschoen.radius.R
import com.ryanschoen.radius.databinding.FragmentMapBinding
import com.ryanschoen.radius.databinding.VenueInfoWindowBinding
import com.ryanschoen.radius.domain.Venue
import com.ryanschoen.radius.yelpRatingToImageRes
import timber.log.Timber

class VenueInfoWindowAdapter(context: Context, private val binding: VenueInfoWindowBinding, private val mapWrapperLayout: MapWrapperLayout) : InfoWindowAdapter {

    override fun getInfoContents(marker: Marker): View? {
        marker.tag?.let {
            //val binding = VenueInfoWindowBinding.inflate(layoutInflater)
            val venue = marker.tag as Venue
            binding.venue = venue
            binding.executePendingBindings()
            binding.infoWindowVisitedCheckbox.isChecked = venue.visited
            //binding.venueName.text = venue.name
            //binding.reviewCount.text = venue.reviews.toString()
            //binding.ratingStars.setImageResource(yelpRatingToImageRes(venue.rating))
            if (venue.id == "9yM20-7fj4LMrOO30nqkBw") {
                Timber.i("Found our venue. We're about to execute bindings with visited = ${binding.infoWindowVisitedCheckbox.isChecked}")
            }
            mapWrapperLayout.setMarkerWithInfoWindow(marker, binding.root)
            return binding.root
        }
        return null
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }
}
/*
map.setInfoWindowAdapter(new InfoWindowAdapter() {


    @Override
    public View getInfoContents(Marker marker) {
        // Setting up the infoWindow with current's marker info
        infoTitle.setText(marker.getTitle());
        infoSnippet.setText(marker.getSnippet());
        infoButtonListener.setMarker(marker);

        // We must call this to set the current marker and infoWindow references
        // to the MapWrapperLayout
        mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
        return infoWindow;
    }
});*/