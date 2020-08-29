package com.behere.loc_based_reminder.data.remote

import android.app.DownloadManager
import com.behere.loc_based_reminder.TodoDao
import com.behere.loc_based_reminder.data.response.Item

class StoreListServiceRepository(
    private val storeListServiceRemoteDataSource:
    StoreListServiceRemoteDataSource,
    private val todoDao: TodoDao
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
        radius: Int,
        cx: Float,
        cy: Float,
        numOfRows: Int,
        success: (List<Item>) -> Unit,
        fail: (String) -> Unit,
        vararg queries: String
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

    fun getPlace(): List<String> {
        val list = ArrayList<String>()
        for (todo in todoDao.getAll()) {
            todo.doPlace?.let {
                list.add(it)
            }
        }
        return list
    }

}