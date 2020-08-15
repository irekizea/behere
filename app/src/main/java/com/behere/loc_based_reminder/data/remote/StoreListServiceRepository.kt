package com.behere.loc_based_reminder.data.remote

import android.app.DownloadManager
import com.behere.loc_based_reminder.data.response.Item

class StoreListServiceRepository(
    private val storeListServiceRemoteDataSource:
    StoreListServiceRemoteDataSource
) {

    private fun getAllStoreListNearBy(
        radius: Int, cx: Float, cy: Float, numOfRows: Int,
        success: (List<Item>) -> Unit,
        fail: (String) -> Unit
    ) {
        storeListServiceRemoteDataSource.getStoreList(
            radius,
            cx,
            cy,
            numOfRows,
            pageNo = 1,
            success = success,
            fail = fail
        )
    }

    fun getToDoStoreListNearBy(
        vararg queries: String,
        radius: Int,
        cx: Float,
        cy: Float,
        numOfRows: Int,
        success: (List<Item>) -> Unit,
        fail: (String) -> Unit
    ) {
        val list = ArrayList<Item>()
        getAllStoreListNearBy(radius, cx, cy, numOfRows, success = {
            for (item in it) {
                for (query in queries) {
                    if (item.bizesNm.contains(query)) {
                        list.add(item)
                    }
                }
            }
            success(list)
        }, fail = {
            fail(it)
        })
    }

    fun getToDoStoreListNearByString(
        radius: Int,
        cx: Float,
        cy: Float,
        numOfRows: Int
    ) {
        storeListServiceRemoteDataSource.getStoreListString(radius, cx, cy, numOfRows, 1)
    }

}