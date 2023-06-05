package uk.ac.ic.doc.aware.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    fun getInstance(): Retrofit {
        val mHttpLoggingInterceptor = HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
        val mOkHttpClient = OkHttpClient.Builder()
            .addInterceptor(mHttpLoggingInterceptor)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://djangobackendapi.herokuapp.com")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(mOkHttpClient)
            .build()
    }
}