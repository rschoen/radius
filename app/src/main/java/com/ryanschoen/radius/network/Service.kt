package com.ryanschoen.radius.network

import com.ryanschoen.radius.BuildConfig.YELP_API_KEY
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

const val RESULTS_PER_QUERY = 50
const val QUERIES_PER_CATEGORY = 4
val CATEGORIES = listOf("restaurant","bar")
const val MINIMUM_REVIEWS = 10


interface VenueService {
    @Headers("Authorization: Bearer $YELP_API_KEY")
    @GET("search?sort_by=distance")
suspend fun getVenues(@Query("location") address: String, @Query("term") searchTerm: String, @Query("limit") limit: Int, @Query("offset") offset: Int): NetworkYelpSearchResults
}

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

object Network {
    private val logging = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val httpClient = OkHttpClient.Builder().addInterceptor(logging)


   private val venueRetrofit = Retrofit.Builder()
        .baseUrl("https://api.yelp.com/v3/businesses/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
       .client(httpClient.build())
        .build()

    val venueList: VenueService = venueRetrofit.create(VenueService::class.java)

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
                    val newVenues = Network.venueList.getVenues(address, category, RESULTS_PER_QUERY, i*RESULTS_PER_QUERY).businesses
                    for (venue in newVenues) {
                        if(!venuesAdded.contains(venue.id) && !venue.closed && venue.reviews.toInt() >= MINIMUM_REVIEWS) {
                            //Timber.i("Calculated distance to be %f",venue.distance)
                            venues.add(venue)
                            venuesAdded.add(venue.id)
                        }
                    }
                }
            }

            Timber.i("Retrieved ${venues.size} venues with $queriesMade queries")

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
