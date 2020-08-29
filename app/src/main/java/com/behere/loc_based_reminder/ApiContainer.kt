package com.behere.loc_based_reminder

import android.content.Context
import com.behere.loc_based_reminder.data.remote.STORE_LIST_SERVICE_BASE_URL
import com.behere.loc_based_reminder.data.remote.StoreListService
import com.behere.loc_based_reminder.data.remote.StoreListServiceRemoteDataSource
import com.behere.loc_based_reminder.data.remote.StoreListServiceRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class ApiContainer(private val context: Context) {
    private val interceptor = HttpLoggingInterceptor().also {
        it.level = HttpLoggingInterceptor.Level.BODY
    }

    private val client =
        OkHttpClient.Builder().addInterceptor(interceptor).build()


    private val storeListService = Retrofit.Builder()
        .baseUrl(STORE_LIST_SERVICE_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(StoreListService::class.java)

    val todoDao =  TodoDB.getInstance(context)?.todoDao()!!
    private val storeListServiceRemoteDataSource = StoreListServiceRemoteDataSource(storeListService)
    val storeListServiceRepository = StoreListServiceRepository(storeListServiceRemoteDataSource, todoDao)
}