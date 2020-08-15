package com.behere.loc_based_reminder.data.remote

import com.behere.loc_based_reminder.data.response.Response
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import java.net.URLDecoder

const val STORE_LIST_SERVICE_BASE_URL = "http://apis.data.go.kr/B553077/api/open/sdsc/"
val STORE_LIST_SERVICE_KEY: String? = URLDecoder.decode("oHRM3LmzGC5b3wvDiHHv71TdYCcs50DkzlRlvBah21L5rtIjzDeNugOGm5mSvmOIlxdKerwEn2x8iA1M45hpeQ%3D%3D", "UTF-8")

//http://apis.data.go.kr/B553077/api/open/sdsc/storeListInRadius?radius=100&cx=126.935539&cy=37.555512&ServiceKey=oHRM3LmzGC5b3wvDiHHv71TdYCcs50DkzlRlvBah21L5rtIjzDeNugOGm5mSvmOIlxdKerwEn2x8iA1M45hpeQ%3D%3D&numOfRows=100&pageNo=4&type=json
interface StoreListService {
    @GET("storeListInRadius")
    fun requestStoreListInRadius(
        @Query("radius") radius: Int,
        @Query("cx") cx: Float,
        @Query("cy") cy: Float,
        @Query("ServiceKey") serviceKey: String = STORE_LIST_SERVICE_KEY?: "",
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int,
        @Query("type") type:String = "json"
    ): Call<Response>

    @GET("storeListInRadius")
    fun requestStoreListInRadiusString(
        @Query("radius") radius: Int,
        @Query("cx") cx: Float,
        @Query("cy") cy: Float,
        @Query("ServiceKey") serviceKey: String = STORE_LIST_SERVICE_KEY?: "",
        @Query("numOfRows") numOfRows: Int,
        @Query("pageNo") pageNo: Int,
        @Query("type") type:String = "json"
    ): Call<String>
}