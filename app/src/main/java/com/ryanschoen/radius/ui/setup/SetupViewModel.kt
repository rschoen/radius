package com.ryanschoen.radius.ui.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ryanschoen.radius.network.Network
import com.ryanschoen.radius.network.NetworkAddress
import com.ryanschoen.radius.network.asNetworkValidationAddress
import com.ryanschoen.radius.repository.getRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

class SetupViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = getRepository(application)

    init {
        if (repo.hasSavedAddress()) {
            Timber.i("Found saved address: " + repo.getSavedAddress())
        }
        else {
            Timber.i("No saved address :(")
        }
    }

    fun verifyAddress(address: String, locality: String, administrativeArea: String, regionCode: String = "US") {
        viewModelScope.launch {
            verifyAddress(
                NetworkAddress(
                    listOf<String>(address),
                    locality,
                    administrativeArea,
                    regionCode
                )
            )
        }
    }

    suspend fun verifyAddress(address: NetworkAddress) {
        withContext(Dispatchers.IO) {
            try {
                val addressResult = Network.addressValidator.validateAddress(address.asNetworkValidationAddress())
                Timber.i(addressResult.toString())
            }
            catch (e: HttpException) {
                Timber.e(e.message())
                Timber.e(e.response()?.message())
                Timber.e(e.code().toString())
                Timber.e(e.stackTraceToString())
            }
        }
    }
}