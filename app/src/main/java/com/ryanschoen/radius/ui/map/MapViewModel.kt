package com.ryanschoen.radius.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ryanschoen.radius.repository.getRepository
import timber.log.Timber

public class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = getRepository(application)
    init {
        if (repo.hasSavedAddress()) {
            Timber.i("Found saved address: " + repo.getSavedAddress())
        }
        else {
            Timber.i("No saved address :(")
        }
    }
}