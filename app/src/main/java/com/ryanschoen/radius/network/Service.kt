package com.ryanschoen.radius.network

import com.ryanschoen.radius.BuildConfig.PLACES_API_KEY
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import timber.log.Timber

const val QUERIES_PER_CATEGORY = 3
val CATEGORIES = listOf("restaurant", "bar")
const val MINIMUM_REVIEWS = 5


interface VenueService {
    @Headers("Referer: ryanschoen.com")
    @GET("json?rankby=distance&key=${PLACES_API_KEY}")
    suspend fun getVenues(
        @Query("location") location: String,
        @Query("type") venueType: String,
        @Query("pagetoken") pageToken: String
    ): NetworkSearchResults
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

object Network {
    //private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val httpClient = OkHttpClient.Builder()//.addInterceptor(logging)


    private val venueRetrofit = Retrofit.Builder()
        .baseUrl("https://maps.googleapis.com/maps/api/place/nearbysearch/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(httpClient.build())
        .build()

    val venueList: VenueService = venueRetrofit.create(VenueService::class.java)

}

suspend fun fetchVenues(lat: Double, lng: Double): NetworkSearchResults {
    val venues = mutableListOf<NetworkVenue>()
    val venuesAdded = mutableListOf<String>()
    val location = "$lat,$lng"
    withContext(Dispatchers.IO) {
        try {
            var queriesMade = 0
            for (category in CATEGORIES) {
                var pageToken = ""
                for (i in 0 until QUERIES_PER_CATEGORY) {
                    queriesMade++
                    val networkResults = Network.venueList.getVenues(
                        location,
                        category,
                        pageToken
                    )
                    for (venue in networkResults.results) {
                        if (!venuesAdded.contains(venue.id) && (venue.reviews ?: 0) >= MINIMUM_REVIEWS) {
                            venues.add(venue)
                            venuesAdded.add(venue.id)
                        }
                    }
                    networkResults.nextPageToken?.let {
                        pageToken = networkResults.nextPageToken
                        Thread.sleep(1500)
                    } ?: {
                        pageToken = ""
                    }
                }
            }

            Timber.i("Retrieved ${venues.size} venues with $queriesMade queries")

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
    return NetworkSearchResults(venues, nextPageToken = "")
}
