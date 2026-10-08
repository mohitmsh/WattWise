package edu.monash.wattwise.data.remote

import edu.monash.wattwise.constants.WeatherApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/** Singleton holding the one Retrofit instance used by the app. */
object RetrofitObject {

    // "by lazy" delays building Retrofit until the first weather request.
    val retrofitService: RetrofitInterface by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(WeatherApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(RetrofitInterface::class.java)
    }
}
