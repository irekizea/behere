package com.behere.loc_based_reminder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val application = application as CommonApplication
        application.apiContainer.storeListServiceRepository
            .getToDoStoreListNearBy("다이소", 100,126.935539f, 37.555512f, 1000, success = {
                Log.d("API", "Success Result $it")
            }, fail = {
                Log.d("API", "Fail Result $it")
            })

        //application.apiContainer.storeListServiceRepository.getToDoStoreListNearByString(100,126.935539f, 37.555512f, 1000)
    }
}