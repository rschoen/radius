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

@BindingAdapter("ratingImage1")
fun ImageView.setRatingImage1(item: Venue?) {
    item?.let {
        setImageResource(ratingStarsToImage(item.rating, 1))
    }
}

@BindingAdapter("ratingImage2")
fun ImageView.setRatingImage2(item: Venue?) {
    item?.let {
        setImageResource(ratingStarsToImage(item.rating,2 ))
    }
}

@BindingAdapter("ratingImage3")
fun ImageView.setRatingImage3(item: Venue?) {
    item?.let {
        setImageResource(ratingStarsToImage(item.rating,3))
    }
}

@BindingAdapter("ratingImage4")
fun ImageView.setRatingImage4(item: Venue?) {
    item?.let {
        setImageResource(ratingStarsToImage(item.rating, 4))
    }
}

@BindingAdapter("ratingImage5")
fun ImageView.setRatingImage5(item: Venue?) {
    item?.let {
        setImageResource(ratingStarsToImage(item.rating, 5))
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