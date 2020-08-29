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
import com.google.android.gms.location.*
import org.json.JSONArray

const val TAG = "TODO"
const val FIND_ACTION = "com.behere.loc_based_reminder.FIND_ACTION"
const val FILE_NAME = "find.json"
const val NOTI_GROUP = "com.behere.loc_based_reminder.NOTI_GROUP"

class LocationUpdatingService : Service() {

    companion object

    var serviceIntent: Intent? = null

    private val ANDROID_CHANNEL_ID = "my.kotlin.application.test200812"
    private val STICK_NOTIFICATION_ID = 1
    private val EVENT_SUMMARY_ID = 0
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
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceIntent = intent

        //Run the service in a different thread
        val handlerThread = HandlerThread("TODO Thread")
        handlerThread.start()

        val handler = HandlerWithLooper(handlerThread.looper)
        handler.post(Runnable {
            requestLocationUpdateByFLC()
            requestLocationUpdateByLM()
        })

        //Stick a Notification for Foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
            createStickNotification(NotificationMode.normal)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun requestLocationUpdateByLM() {
        val INTERVAL = (1000 * 10).toLong() // 10sec
        val DISTANCE = 1.toFloat() // 1m

        locationListener = object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.e(
                    TAG,
                    "[location(LM)] latitude: ${location.latitude} longitude: ${location.longitude}"
                )

                val application = application as CommonApplication
                val queries = application.apiContainer.storeListServiceRepository?.getPlace()
                if (queries.isNullOrEmpty()) return
                val temp = queries.toTypedArray()
                application.apiContainer.storeListServiceRepository
                    .getToDoStoreListNearBy(
                        20,
                        location.longitude.toFloat(),
                        location.latitude.toFloat(),
                        1000,
                        success = {
                            Log.e(TAG, "Success Result $it")
                            val arr = JSONArray()
                            val map = HashMap<String, ArrayList<Item>>()
                            var id = EVENT_NOTIFICATION_ID
                            //TODO:점포 이름 다르게 들어오는 거 확인.
                            for (item in it) {
                                for (q in queries) {
                                    Log.e("다원", "query : $q")
                                    if (item.bizesNm.contains(q)) {
                                        if (!map.containsKey(q)) {
                                            Log.e("다원", "query not contains : $q")
                                            map[q] = ArrayList<Item>()
                                        }
                                        map[q]?.add(item)
                                    }
                                }
                            }
                            for (value in map) {
                                Log.e("다원", "noti : ${value.key}")
                                with(NotificationManagerCompat.from(applicationContext))
                                {
                                    notify(
                                        id,
                                        setEventNotification(
                                            location,
                                            value.key,
                                            value.value,
                                            id
                                        )!!.build()
                                    )
                                }
                                id += 1
                            }

                            if (map.size > 1) {
                                Log.e("다원", "noti summary")
                                val summaryNotification = NotificationCompat.Builder(
                                    applicationContext,
                                    ANDROID_CHANNEL_ID
                                )
                                    .setContentTitle("근접 알림")
                                    .setContentText("${map.size}개의 알림이 있습니다.")
                                    .setSmallIcon(R.drawable.alarm)
                                    .setGroup(NOTI_GROUP)
                                    .setGroupSummary(true)
                                    .build()

                                with(NotificationManagerCompat.from(applicationContext)) {
                                    notify(EVENT_SUMMARY_ID, summaryNotification)
                                }
                            }

                            //writeFile(FILE_NAME, applicationContext, arr.toString())
                            //알림 표시
                        },
                        fail = {
                            Log.e(TAG, "Fail Result $it")
                        },
                        queries = *temp
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

    private fun requestLocationUpdateByFLC() {
        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            smallestDisplacement = 1f //lm
            fastestInterval = 10 * 1000 //10sec
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult ?: createStickNotification(NotificationMode.issue)

                for (location in locationResult!!.locations) {

                    Log.e(
                        TAG,
                        "[location(FLC)] latitude: ${location.latitude} longitude: ${location.longitude}"
                    )

                    val application = application as CommonApplication
                    val queries = application.apiContainer.storeListServiceRepository?.getPlace()
                    if (queries.isNullOrEmpty()) return
                    val temp = queries.toTypedArray()
                    application.apiContainer.storeListServiceRepository
                        .getToDoStoreListNearBy(
                            20,
                            location.longitude.toFloat(),
                            location.latitude.toFloat(),
                            1000,
                            success = {
                                Log.e(TAG, "Success Result $it")
                                val arr = JSONArray()
                                val map = HashMap<String, ArrayList<Item>>()
                                var id = EVENT_NOTIFICATION_ID
                                //TODO:점포 이름 다르게 들어오는 거 확인.
                                for (item in it) {
                                    for (q in queries) {
                                        Log.e(TAG, "query : $q")
                                        if (item.bizesNm.contains(q)) {
                                            if (!map.containsKey(q)) {
                                                Log.e(TAG, "query not contains : $q")
                                                map[q] = ArrayList<Item>()
                                            }
                                            map[q]?.add(item)
                                        }
                                    }
                                }
                                for (value in map) {
                                    Log.e(TAG, "noti : ${value.key}")
                                    with(NotificationManagerCompat.from(applicationContext))
                                    {
                                        notify(
                                            id,
                                            setEventNotification(
                                                location,
                                                value.key,
                                                value.value,
                                                id
                                            )!!.build()
                                        )
                                    }
                                    id += 1
                                }

                                if (map.size > 1) {
                                    Log.e("다원", "noti summary")
                                    val summaryNotification = NotificationCompat.Builder(
                                        applicationContext,
                                        ANDROID_CHANNEL_ID
                                    )
                                        .setContentTitle("근접 알림")
                                        .setContentText("${map.size}개의 알림이 있습니다.")
                                        .setSmallIcon(R.drawable.alarm)
                                        .setGroup(NOTI_GROUP)
                                        .setGroupSummary(true)
                                        .build()

                                    with(NotificationManagerCompat.from(applicationContext)) {
                                        notify(EVENT_SUMMARY_ID, summaryNotification)
                                    }
                                }

                                //writeFile(FILE_NAME, applicationContext, arr.toString())
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

    private fun createStickNotification(notificationMode: NotificationMode) {
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
        startForeground(STICK_NOTIFICATION_ID, notification)
    }

    private fun setEventNotification(
        location: Location,
        q: String,
        items: ArrayList<Item>,
        id: Int
    ): NotificationCompat.Builder {
        //알림 클릭 시, 앱 진입
        val intent = Intent(this, MapActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = FIND_ACTION
            putParcelableArrayListExtra("items", items)
            putExtra("lat", location.latitude)
            putExtra("lng", location.longitude)
        }

        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        //Set notification content
        return NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
            .setSmallIcon(R.drawable.alarm)
            .setContentTitle(q)
            .setContentText("할 일 설정 장소 $q 가 인접한 곳에 있습니다.")
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(NOTI_GROUP)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
    }
}