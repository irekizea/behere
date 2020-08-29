package com.behere.loc_based_reminder.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.behere.loc_based_reminder.CommonApplication
import com.behere.loc_based_reminder.MapActivity
import com.behere.loc_based_reminder.R
import com.behere.loc_based_reminder.data.response.Item
import com.behere.loc_based_reminder.util.writeFile
import com.google.android.gms.location.*
import org.json.JSONArray

const val TAG = "TODO"
const val FIND_ACTION = "com.behere.loc_based_reminder.FIND_ACTION"
const val FILE_NAME = "find.json"
const val NOTI_GROUP = "com.behere.loc_based_reminder.NOTI_GROUP"

class LocationUpdatingService : Service() {

    companion object var serviceIntent: Intent? = null

    private val ANDROID_CHANNEL_ID = "my.kotlin.application.test200812"
    private val FOREGROUND_NOTIFICATION_ID = 1
    private val EVENT_SUMMARY_ID = 9
    private val EVENT_NOTIFICATION_ID = 9

    private var notificationManager: NotificationManager? = null

    //Location callback by FLC(FusedLocationProviderClient)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    //Location callback by LM(LocationManager)
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: android.location.LocationListener

    private enum class NotificationMode {
        normal, issue
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this) //for FLC
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager //for LM

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Run the service in a different thread
        val handlerThread = HandlerThread("TODO Thread")
        handlerThread.start()

        val handler = HandlerWithLooper(handlerThread.looper)
        handler.post(Runnable {
            requestLocationUpdateByFLC()
        })

        //Stick a Notification for Foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            createForegroundNotification(NotificationMode.normal)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceIntent = intent
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun requestLocationUpdateByLM() {
        val INTERVAL = 1000.toLong() // 1sec
        val DISTANCE = 1.toFloat() // 1m

        locationListener = object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.e(
                    TAG,
                    "[location(LM)] latitude: ${location.latitude} longitude: ${location.longitude}"
                )
            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
            }

            override fun onProviderEnabled(p0: String?) {
            }

            override fun onProviderDisabled(p0: String?) {
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "No location permission")
            return
        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                INTERVAL,
                DISTANCE,
                locationListener
            )
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                INTERVAL,
                DISTANCE,
                locationListener
            )
        }
    }

    fun requestLocationUpdateByFLC() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//            interval = 5 * 1000
            fastestInterval = 1 * 1000 // 1sec
            smallestDisplacement = 1.toFloat() // 1m
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult ?: createForegroundNotification(NotificationMode.issue)

                for (location in locationResult!!.locations) {

                    Log.e(
                        TAG,
                        "[location(FLC)] latitude: ${location.latitude} longitude: ${location.longitude}"
                    )

                    var storeList = mutableListOf<String>()

                    val application = application as CommonApplication
                    val queries = ArrayList<String>()
                    queries.add("다이소")
                    queries.add("GS25")
                    queries.add("편의점")
                    val temp = queries.toTypedArray()
                    application.apiContainer.storeListServiceRepository
                        .getToDoStoreListNearBy(
                            500,
                            location.longitude.toFloat(),
                            location.latitude.toFloat(),
                            100,
                            success = {
                                Log.e(TAG, "Success Result $it")
                                val arr = JSONArray()
                                var id = EVENT_NOTIFICATION_ID
//                                for (item in it) {
//                                    with(NotificationManagerCompat.from(applicationContext)) {
//                                        notify(id, setEventNotification(item, id)!!.build())
//                                    }
//                                    id += 1
//                                }

                                //TODO:점포 이름 다르게 들어오는 거 확인.
                                for (item in it) {
                                    Log.e(TAG, "${storeList.toString()}")
                                    if (id == EVENT_NOTIFICATION_ID) {
                                        storeList.add(item.bizesNm)
                                        with(NotificationManagerCompat.from(applicationContext))
                                        {
                                            notify(id, setEventNotification(item, id)!!.build())
                                        }
                                        id++
                                    } else {
                                        if (!storeList!!.contains(item.bizesNm)) {
                                            storeList.add(item.bizesNm)
                                            with(NotificationManagerCompat.from(applicationContext))
                                            {
                                                notify(id, setEventNotification(item, id)!!.build())
                                            }
                                            id++
                                        }
                                    }
                                }

                                val summaryNotification = NotificationCompat.Builder(
                                    applicationContext,
                                    ANDROID_CHANNEL_ID
                                )
                                    .setContentTitle("근접 알림")
                                    //set content text to support devices running API level < 24
                                    .setContentText("${it.size}개의 알림이 있습니다.")
                                    .setSmallIcon(R.drawable.bell)
                                    //build summary info into InboxStyle template
                                    //specify which group this notification belongs to
                                    .setGroup(NOTI_GROUP)
                                    //set this notification as the summary for the group
                                    .setGroupSummary(true)
                                    .build()

                                with(NotificationManagerCompat.from(applicationContext)) {
                                    notify(EVENT_SUMMARY_ID, summaryNotification)
                                }

                                writeFile(FILE_NAME, applicationContext, arr.toString())
                                //알림 표시
                            },
                            fail = {
                                Log.e(TAG, "Fail Result $it")
                            },
                            queries = *temp
                        )
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "No location permission")
            return
        } else {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    inner class HandlerWithLooper(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Log.e(
                TAG,
                "Thread is running"
            )
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ANDROID_CHANNEL_ID,
                "TODO Service",
                NotificationManager.IMPORTANCE_NONE
            )
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    private fun createForegroundNotification(notificationMode: NotificationMode) {

        //Create notification object
        var notification: Notification? = null

        when (notificationMode) {
            NotificationMode.normal -> {
                val builder = NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
                    .setContentTitle("TO DO")
                    .setContentText("위치 정보 사용 중입니다.")
                    .setSmallIcon(R.drawable.location)
                notification = builder.build()
            }
            NotificationMode.issue -> {
                val builder = NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
                    .setContentTitle("TO DO")
                    .setContentText("위치 권한 추가 허용이 필요합니다.")
                    .setSmallIcon(R.drawable.location)
                notification = builder.build()
            }
        }

        //Service starts with notification pinning
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun setEventNotification(item: Item, id: Int): NotificationCompat.Builder {
        //알림 클릭 시, 앱 진입
        val intent = Intent(this, MapActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = FIND_ACTION
            putExtra("item", item)
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //알림 콘텐츠 설정
        return NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
            .setSmallIcon(R.drawable.bell)
            .setContentTitle("${item.bizesNm}")
            .setContentText("할 일 설정 장소 ${item.bizesNm}가 인접한 곳에 있습니다.")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(NOTI_GROUP)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
    }
}