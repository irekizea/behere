package com.behere.loc_based_reminder.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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
import com.behere.loc_based_reminder.receiver.MyBroadcastReceiver
import com.behere.loc_based_reminder.util.writeFile
import com.google.android.gms.location.*
import org.json.JSONArray


const val FIND_ACTION = "com.behere.loc_based_reminder.FIND_ACTION"
const val FILE_NAME = "find.json"
const val NOTI_GROUP = "com.behere.loc_based_reminder.NOTI_GROUP"
class LocationUpdatingService : Service() {

    private val ANDROID_CHANNEL_ID = "my.kotlin.application.test200812"
    private val FOREGROUND_NOTIFICATION_ID = 1
    private val EVENT_SUMMARY_ID = 9
    private val EVENT_NOTIFICATION_ID = 9

    private var notificationManager: NotificationManager? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: android.location.LocationListener

    //private var eventBuilder: NotificationCompat.Builder? = null

    var receiver: MyBroadcastReceiver? = null

    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val handlerThread = HandlerThread("ReminderThread")
        handlerThread.start()

        //핸들러는 스레드의 looper를 통해 동작.
        //1개의 스레드에 1개의 looper가 존재.

        val handlerForOtherThread = HandlerWithLooper(handlerThread.looper)
        handlerForOtherThread.post(Runnable {
            requestLocationUpdateByFLC()
//            requestLocationUpdateByLM()
        })

//        val handlerForMainThread = HandlerWithLooper(Looper.getMainLooper())
//        handlerForMainThread.post {
//            Thread.sleep(5000)
//            Toast.makeText(applicationContext, "접근 알림!", Toast.LENGTH_LONG).show()
//        }

        //오레오 버전 이상은 포그라운드 서비스(+고정 알림 포함) 설정을 해주어야 함.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            setNotificationChannel()
            setForegroundNotification()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        fusedLocationClient.removeLocationUpdates(locationCallback)
//        locationManager.removeUpdates(locationListener)
    }

    fun requestLocationUpdateByLM() {

        val INTERVAL = 1000.toLong()
        val DISTANCE = 1.toFloat()

        locationListener = object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.e(
                    "우진",
                    "[업데이트된 위치 by LM] 위도: ${location.latitude} 경도: ${location.longitude}"
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
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("우진", "위치 권한 없음")
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
            fastestInterval = 1 * 1000
            smallestDisplacement = 1.toFloat() //기본: 50미터
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult ?: return

                for (location in locationResult.locations) {

                    Log.e(
                        "우진",
                        "[업데이트된 위치 by FLC] 위도: ${location.latitude} 경도: ${location.longitude}"
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
                                Log.e("우진 다원", "Success Result $it")
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
                                    Log.e("우진","${storeList.toString()}")
                                    if(id == EVENT_NOTIFICATION_ID){
                                        storeList.add(item.bizesNm)
                                        with(NotificationManagerCompat.from(applicationContext))
                                        {
                                            notify(id, setEventNotification(item, id)!!.build())
                                        }
                                        id++
                                    }
                                    else{
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

                                val summaryNotification = NotificationCompat.Builder(applicationContext, ANDROID_CHANNEL_ID)
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
                                Log.e("우진 다원", "Fail Result $it")
                            },
                            queries = *temp
                        )
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    inner class HandlerWithLooper(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            Log.e("우진", "스레드 실행 중입니다.")
            when (msg.what) {
                1 -> Log.e("우진", "핸들러 메시지 수신 성공")
            }
        }
    }

    private fun setNotificationChannel() {
        //알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                    ANDROID_CHANNEL_ID,
                "ReminderService",
                NotificationManager.IMPORTANCE_NONE
            )

            //Notification 객체를 가져옴
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    private fun setForegroundNotification() {
        //알림 객체 생성
        val builder = NotificationCompat.Builder(this, ANDROID_CHANNEL_ID)
            .setContentTitle("여기있소")
            .setContentText("위치 정보 사용 중입니다.")
            .setSmallIcon(R.drawable.gps)

        val notification = builder.build()

        //알림과 함께 서비스 시작
        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun setEventNotification(item: Item, id: Int) : NotificationCompat.Builder{
        //알림 클릭 시, 앱 진입
        val intent = Intent(this, MapActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            action = FIND_ACTION
            putExtra("item", item)
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)

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