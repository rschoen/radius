package com.ryanschoen.radius.ui.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import com.ryanschoen.radius.R
import com.ryanschoen.radius.databinding.VenueInfoWindowBinding
import com.ryanschoen.radius.domain.Venue
import com.ryanschoen.radius.yelpRatingToImageRes

class VenueInfoWindowAdapter(context: Context) : InfoWindowAdapter {

    private val layoutInflater = LayoutInflater.from(context)

    override fun getInfoContents(p0: Marker): View {
        val binding = VenueInfoWindowBinding.inflate(layoutInflater)
        val venue = p0.tag as Venue
        binding.venueName.text = venue.name
        binding.reviewCount.text = venue.reviews.toString()
        binding.ratingStars.setImageResource(yelpRatingToImageRes(venue.rating))
        binding.executePendingBindings()
        return binding.root
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }
}