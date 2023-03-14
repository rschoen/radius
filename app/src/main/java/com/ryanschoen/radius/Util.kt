package com.ryanschoen.radius

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import kotlin.math.abs


fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun yelpRatingToImageRes(rating: Double): Int {
    val ratingInt = (rating*2).toInt()
    return when(ratingInt) {
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

fun yelpIntent(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(context, intent, null)
}

fun metersEquals(a: Double, b: Double, delta: Double = 0.1): Boolean {
    return abs(a-b) < delta
}