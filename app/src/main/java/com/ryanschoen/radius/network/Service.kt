package com.ryanschoen.radius.network

import com.ryanschoen.radius.BuildConfig.MAPS_API_KEY
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface AddressValidationService {
    @Headers("Content-Type: application/json")
    @POST("./v1:validateAddress?key=" + MAPS_API_KEY)
    suspend fun validateAddress(@Body address: NetworkValidationAddress): NetworkAddressResult
}

interface VenueService {
    //@GET("") // TODO
    //suspend fun getVenues(address: String): Deferred<NetworkVenuesResult>
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
        //.addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(httpClient.build())
        .build()

    val addressValidator = addressRetrofit.create(AddressValidationService::class.java)

   /* private val venueRetrofit = Retrofit.Builder()
        .baseUrl("") //TODO
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val venueList = venueRetrofit.create(VenueService::class.java)*/

}