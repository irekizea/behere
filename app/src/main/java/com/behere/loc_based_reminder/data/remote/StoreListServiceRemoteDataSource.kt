package com.behere.loc_based_reminder.data.remote

import android.util.Log
import com.behere.loc_based_reminder.data.response.Item
import com.behere.loc_based_reminder.data.response.Response
import retrofit2.Call
import retrofit2.Callback
import retrofit2.http.Query

class StoreListServiceRemoteDataSource(private val storeListService: StoreListService) {
    val totalList = ArrayList<Item>()
    var page = 1

    fun init() {
        totalList.clear()
        page = 1
    }
    fun getStoreList(
        radius: Int,
        cx: Float,
        cy: Float,
        numOfRows: Int,
        pageNo: Int,
        success: (List<Item>) -> Unit,
        fail: (String) -> Unit
    ) {
        storeListService.requestStoreListInRadius(
            radius,
            cx,
            cy,
            numOfRows = numOfRows,
            pageNo = page
        ).enqueue(object : Callback<Response> {
            override fun onResponse(call: Call<Response>, response: retrofit2.Response<Response>) {
                if (!response.isSuccessful) fail("Response is not successful")
                Log.d("API", "Response Success ${response.body()}")
                Log.d("API", "Response page ${page}")
                response.body()?.let { response ->
                    Log.d("API", "Response ResultCode ${response.header.resultCode}")
                    if (response.header.resultCode == "03") {
                        // 데이터가 없음
                        success(totalList)
                        return
                    }
                    totalList.addAll(response.body.items)
                    page += 1
                    getStoreList(radius, cx, cy, numOfRows, pageNo, success, fail)
                }
            }

            override fun onFailure(call: Call<Response>, t: Throwable) {
                t.message?.let {
                    fail(it)
                }

            }
        })
    }

    fun getStoreListString(
        radius: Int,
        cx: Float,
        cy: Float,
        numOfRows: Int,
        pageNo: Int
    ) {
        val totalList = ArrayList<Item>()
        var page = pageNo
        storeListService.requestStoreListInRadiusString(
            radius,
            cx,
            cy,
            numOfRows = numOfRows,
            pageNo = page
        ).enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>, response: retrofit2.Response<String>) {
                if (!response.isSuccessful) return
                Log.d("API", "Response Success ${response.body()}")

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                t.message?.let {
                }

            }
        })
    }
}