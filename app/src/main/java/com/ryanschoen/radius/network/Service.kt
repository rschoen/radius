package com.ryanschoen.radius.network

import com.ryanschoen.radius.BuildConfig.RADIUS_API_KEY
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import timber.log.Timber

const val MINIMUM_REVIEWS = 5


interface VenueService {
    @Headers("Referer: ryanschoen.com")
    @GET("nearby?key=${RADIUS_API_KEY}")
    suspend fun getVenues(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
    ): RadiusAPIResult
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

object Network {
    //private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val httpClient = OkHttpClient.Builder()//.addInterceptor(logging)


    private val venueRetrofit = Retrofit.Builder()
        .baseUrl("https://fellyeah.duckdns.org:3491/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(httpClient.build())
        .build()

    val venueList: VenueService = venueRetrofit.create(VenueService::class.java)

}

suspend fun fetchVenues(lat: Double, lng: Double): RadiusAPIResult {
    val venues = mutableListOf<NetworkVenue>()
    var metadata = RadiusMetadata("",false)

    withContext(Dispatchers.IO) {
        try {
            val networkResults = Network.venueList.getVenues(lat, lng)
            metadata = networkResults.metadata
            for (venue in networkResults.venues) {
                if ((venue.reviews ?: 0) >= MINIMUM_REVIEWS) {
                    venues.add(venue)
                }
            }

            Timber.i("Retrieved ${venues.size} venues ")

        } catch (e: HttpException) {
            //TODO: do something more meaningful with this error
            //Toast.makeText(getApplication(),"Address validation encountered an HTTP error. Please retry.",
            //Toast.LENGTH_LONG).show()
            Timber.e(e)
        } catch (e: Exception) {
            //TODO: do something more meaningful with this error
            //Toast.makeText(getApplication(),"Address validation encountered an unknown error. Please retry.",
            //Toast.LENGTH_LONG).show()
            Timber.e(e)
        }

    }
    return RadiusAPIResult(metadata, venues)
}
