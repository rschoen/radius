package com.ryanschoen.radius.ui.venues

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ryanschoen.radius.domain.Venue
import com.ryanschoen.radius.yelpRatingToImageRes
import java.text.NumberFormat
import java.util.*

@BindingAdapter("reviewString")
fun TextView.setReviewString(item: Venue?) {
    item?.let {
        text = withCommas(item.reviews) + " reviews"
    }
}


@BindingAdapter("ratingImage")
fun ImageView.setRatingImage(item: Venue?) {
    item?.let {
        setImageResource(yelpRatingToImageRes(item.rating))
    }
}

@BindingAdapter("reviewText")
fun TextView.setReviewTextJustNumber(item: Venue?) {
    item?.let {
        text = withCommas(item.reviews)
    }
}

fun withCommas(num: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(num)
}