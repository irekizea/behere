package com.behere.loc_based_reminder

import android.app.Application

class CommonApplication : Application() {
    lateinit var apiContainer: ApiContainer
    override fun onCreate() {
        super.onCreate()
        apiContainer = ApiContainer()

        //http://apis.data.go.kr/B553077/api/open/sdsc/storeListInRadius?radius=100&cx=126.935539&cy=37.555512&ServiceKey=oHRM3LmzGC5b3wvDiHHv71TdYCcs50DkzlRlvBah21L5rtIjzDeNugOGm5mSvmOIlxdKerwEn2x8iA1M45hpeQ%3D%3D&numOfRows=100&pageNo=4&type=json

    }
}