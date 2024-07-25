package com.ryanschoen.radius

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import kotlin.math.abs


fun yelpRatingToImageRes(rating: Double): Int {
    return when ((rating * 2).toInt()) {
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
}

fun venueDetailsIntent(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(context, intent, null)
}

fun metersEquals(a: Double, b: Double, delta: Double = 0.001): Boolean {
    return abs(a - b) < delta
}

fun metersToMiles(m: Double): Double {
    return m / 1609.344
}

fun metersBetweenPoints(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val results = FloatArray(1)
    Location.distanceBetween(lat1, lng1, lat2, lng2, results)
    return results[0].toDouble()
}

fun getPixelsFromDp(context: Context, dp: Float): Int {
    val scale: Float = context.resources.displayMetrics.density
    return (dp * scale + 0.5f).toInt()
}