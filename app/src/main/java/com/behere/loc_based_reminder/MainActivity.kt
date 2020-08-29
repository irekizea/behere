package com.behere.loc_based_reminder

import android.Manifest
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.DataSetObserver
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.behere.loc_based_reminder.service.LocationUpdatingService
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest

const val TAG = "TODO"

class MainActivity : AppCompatActivity() {

    private var todoDb: TodoDB? = null
    private var todoList = mutableListOf<Todo>()
    lateinit var mAdapter: TodoAdapter

    private val LOCATION_REQUEST_CODE = 999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            val info: PackageInfo = packageManager.getPackageInfo(
                "com.behere.loc_based_reminder",
                PackageManager.GET_SIGNATURES
            )
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                    Log.d("KeyHash : ", Base64.encodeToString(md.digest(), Base64.DEFAULT))
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        //Permission check for user location access
        val isLocationPermissionApproved = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
        if (isLocationPermissionApproved) {
            val isBackgroundLocationPermissionApproved = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (isBackgroundLocationPermissionApproved) {
                //All permissions are granted
            } else {
                //User not allowed background location permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    LOCATION_REQUEST_CODE
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                LOCATION_REQUEST_CODE
            )
        }

        todoDb = TodoDB.getInstance(this)
        mAdapter = TodoAdapter(this, todoList)
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recycler_view)


        val intent = Intent(this, LocationUpdatingService::class.java)
        val r = Runnable {
            try {
                todoList = (todoDb?.todoDao()?.getAll() as MutableList<Todo>?)!!
                mAdapter = TodoAdapter(this, todoList)
                mAdapter.notifyDataSetChanged()

                recycler_view.adapter = mAdapter
                recycler_view.layoutManager = LinearLayoutManager(this)
                recycler_view.setHasFixedSize(true)
            } catch (e: Exception) {
                Log.d(TAG, "Error - $e")
            }

            if (LocationUpdatingService().serviceIntent != null) {
                if (todoList.size == 0) {
                    stopService(intent)
                    Log.e(TAG, "Stop Service because No schedule")
                }
            } else {
                // At least one schedule
                if (todoList.size > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        //오레오 이상은 백그라운드로 실행하면 강제 종료 위험 있음 -> 포그라운드 실행해야
                        startForegroundService(
                            Intent(
                                applicationContext,
                                LocationUpdatingService::class.java
                            )
                        )
                        Log.e(TAG, "API 레벨 26 이상")
                    } else {
                        //백그라운드 실행에 제약 없음
                        startService(
                            Intent(
                                applicationContext,
                                LocationUpdatingService::class.java
                            )
                        )
                        Log.e("우진", "API 레벨 25 이하")
                    }
                } else {
                }
                todoList.forEach {
                    Log.e(TAG, it.doTodo)
                }
            }
        }

        val thread = Thread(r)
        thread.start()
        setViews()
    }


    override fun onDestroy() {
        TodoDB.destroyInstance()
        todoDb = null
        super.onDestroy()
    }

    private fun setViews() {
        add_btn.setOnClickListener {
            val intent = Intent(applicationContext, AddActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    private var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback =
        object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                @NonNull recyclerView: RecyclerView,
                @NonNull viewHolder: RecyclerView.ViewHolder,
                @NonNull target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(
                @NonNull viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.adapterPosition
                val todo = todoList.removeAt(position)
                mAdapter.notifyItemRemoved(position)
                todoDb?.todoDao()?.delete(todo)
            }
        }

}



