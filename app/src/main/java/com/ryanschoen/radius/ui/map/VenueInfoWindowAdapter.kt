package com.ryanschoen.radius.ui.map

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.model.Marker
import com.ryanschoen.radius.R
import com.ryanschoen.radius.databinding.VenueInfoWindowBinding
import com.ryanschoen.radius.domain.Venue

class VenueInfoWindowAdapter(context: Context) : InfoWindowAdapter {

    private val layoutInflater = LayoutInflater.from(context)

    override fun getInfoContents(p0: Marker): View {
        val binding = VenueInfoWindowBinding.inflate(layoutInflater)
        val venue = p0.tag as Venue
        binding.venueName.text = venue.name
        binding.reviewCount.text = venue.reviews.toString()
        binding.ratingStars.setImageResource(
            when((venue.rating*2).toInt()) {
                2 -> R.drawable.stars_regular_1
                3 -> R.drawable.stars_regular_1_half
                4 -> R.drawable.stars_regular_2
                5 -> R.drawable.stars_regular_2_half
                6 -> R.drawable.stars_regular_3
                7 -> R.drawable.stars_regular_3_half
                8 -> R.drawable.stars_regular_4
                9 -> R.drawable.stars_regular_4_half
                10 -> R.drawable.stars_regular_5
                else -> R.drawable.stars_regular_0
            }
        )
        binding.executePendingBindings()
        return binding.root
    }

    override fun getInfoWindow(p0: Marker): View? {
        return null
    }
}