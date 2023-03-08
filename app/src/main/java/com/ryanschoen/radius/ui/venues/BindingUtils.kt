package com.ryanschoen.radius.ui.venues

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ryanschoen.radius.domain.Venue
import com.ryanschoen.radius.yelpRatingToImageRes

@BindingAdapter("reviewString")
fun TextView.setReviewString(item: Venue?) {
    item?.let {
        text = item.reviews.toString() + " reviews"
    }
}


@BindingAdapter("ratingImage")
fun ImageView.setRatingImage(item: Venue?) {
    item?.let {
        setImageResource(yelpRatingToImageRes(item.rating))
    }
}