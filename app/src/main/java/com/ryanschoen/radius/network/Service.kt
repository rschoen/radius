package com.ryanschoen.radius.network

import com.ryanschoen.radius.BuildConfig.MAPS_API_KEY
import com.ryanschoen.radius.BuildConfig.YELP_API_KEY
import com.ryanschoen.radius.domain.AddressResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import timber.log.Timber

val RESULTS_PER_QUERY = 50
val QUERIES_PER_CATEGORY = 1
val CATEGORIES = listOf("restaurant","bar")
val MINIMUM_REVIEWS = 10


interface AddressValidationService {
    @Headers("Content-Type: application/json")
    @POST("./v1:validateAddress?key=" + MAPS_API_KEY)
    suspend fun validateAddress(@Body address: _NetworkValidationAddress): NetworkAddressResult
}

interface VenueService {
    @Headers("Authorization: Bearer " + YELP_API_KEY)
    @GET("search?sort_by=distance")
suspend fun getVenues(@Query("location") address: String, @Query("term") searchTerm: String, @Query("limit") limit: Int, @Query("offset") offset: Int): NetworkYelpSearchResults
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

object Network {
    val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    val httpClient = OkHttpClient.Builder().addInterceptor(logging)

    private val addressRetrofit = Retrofit.Builder()
        .baseUrl("https://addressvalidation.googleapis.com/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(httpClient.build())
        .build()

    val addressValidator = addressRetrofit.create(AddressValidationService::class.java)

   private val venueRetrofit = Retrofit.Builder()
        .baseUrl("https://api.yelp.com/v3/businesses/") //TODO
        .addConverterFactory(MoshiConverterFactory.create(moshi))
       .client(httpClient.build())
        .build()

    val venueList = venueRetrofit.create(VenueService::class.java)

}

suspend fun sendAddressVerification(address: NetworkAddress): AddressResult? {
    var addressResult: NetworkAddressResult? = null
    withContext(Dispatchers.IO) {
        try {
            addressResult = Network.addressValidator.validateAddress(address.asNetworkValidationAddress())
        }
        catch (e: HttpException) {
            //TODO: do something more meaningful witht his error
            //Toast.makeText(getApplication(),"Address validation encountered an HTTP error. Please retry.",
                //Toast.LENGTH_LONG).show()
            Timber.e(e)
        }
        catch (e: Exception) {
            //TODO: do something more meaningful witht his error
            //Toast.makeText(getApplication(),"Address validation encountered an unknown error. Please retry.",
                //Toast.LENGTH_LONG).show()
            Timber.e(e)
        }
    }

    if(addressResult == null) {
        return null
    } else {
        return addressResult!!.asDomainModel()
    }
}

suspend fun fetchVenues(address: String): NetworkYelpSearchResults {
    val venues = mutableListOf<NetworkVenue>()
    val venuesAdded = mutableListOf<String>()
    withContext(Dispatchers.IO) {
        try {
            var queriesMade = 0
            for(category in CATEGORIES) {
                for(i in 0 until QUERIES_PER_CATEGORY) {
                    queriesMade++
                    val newVenues = Network.venueList.getVenues(address, category, RESULTS_PER_QUERY, i).businesses
                    for (venue in newVenues) {
                        if(!venuesAdded.contains(venue.id) && !venue.closed && venue.reviews.toInt() >= MINIMUM_REVIEWS) {
                            //Timber.i("Calculated distance to be %f",venue.distance)
                            venues.add(venue)
                            venuesAdded.add(venue.id)
                        }
                    }
                }
            }

            Timber.i("Retrieved ${venues.size} venues with ${queriesMade} queries")

        }
        catch (e: HttpException) {
            //TODO: do something more meaningful with this error
            //Toast.makeText(getApplication(),"Address validation encountered an HTTP error. Please retry.",
            //Toast.LENGTH_LONG).show()
            Timber.e(e)
        }
        catch (e: Exception) {
            //TODO: do something more meaningful with this error
            //Toast.makeText(getApplication(),"Address validation encountered an unknown error. Please retry.",
            //Toast.LENGTH_LONG).show()
            Timber.e(e)
        }

    }
    return NetworkYelpSearchResults(venues)
}
