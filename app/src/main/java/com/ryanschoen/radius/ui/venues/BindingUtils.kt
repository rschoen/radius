package com.ryanschoen.radius.ui.venues

import android.graphics.Typeface
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.ryanschoen.radius.R
import com.ryanschoen.radius.domain.Venue
import com.ryanschoen.radius.metersToMiles
import com.ryanschoen.radius.ratingStarsToImage
import java.text.NumberFormat
import java.util.*


@BindingAdapter("reviewString")
fun TextView.setReviewString(item: Venue?) {
    item?.let {
        text = String.format(context.getString(R.string.reviews_count), withCommas(item.reviews))
    }
}


@BindingAdapter("ratingImage")
fun ImageView.setRatingImage(item: Venue?) {
    item?.let {
        setImageResource(ratingStarsToImage(item.rating))
    }
}

@BindingAdapter("reviewText")
fun TextView.setReviewTextJustNumber(item: Venue?) {
    item?.let {
        text = withCommas(item.reviews)
    }
}

@BindingAdapter("distanceMiles")
fun TextView.metersToMilesDisplay(item: Venue?) {
    item?.let {
        text = String.format("%.2f mi", metersToMiles(item.distance))
    }
}

@BindingAdapter("nameWithHidden")
fun TextView.nameWithHidden(item: Venue?) {
    item?.let {
        if (item.hidden) {
            text = String.format(context.getString(R.string.venue_name_hidden), item.name)
            setTypeface(typeface, Typeface.ITALIC)
        } else {
            text = item.name
            setTypeface(typeface, Typeface.NORMAL)
        }
    }
}

fun withCommas(num: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(num)
}