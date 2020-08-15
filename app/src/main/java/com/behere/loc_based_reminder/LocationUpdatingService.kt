package com.behere.loc_based_reminder

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*


class LocationUpdatingService : Service() {

    private var serviceIntent: Intent? = null

    private val ANDROID_CHANNEL_ID = "my.kotlin.application.test200812"
    private val NOTIFICATION_ID = 1

    private var notificationManager: NotificationManager? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: android.location.LocationListener

    var receiver: MyBroadcastReceiver? = null

    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("우진", "서비스 실행 in onCreate()")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //스레드 테스트
        Log.e("우진", "UI Thread 동작")

        val handlerThread = HandlerThread("LocationServiceThread")
        handlerThread.start()

        //핸들러는 스레드의 looper를 통해 동작.
        //1개의 스레드에 1개의 looper가 존재.

        //Toast 알림 띄우기
//        val handlerWithLooper = HandlerWithLooper(applicationContext.mainLooper)
//        handlerWithLooper.post(Runnable { Toast.makeText(applicationContext, "접근 알림!", Toast.LENGTH_LONG).show() })

        val handlerForOtherThread = HandlerWithLooper(handlerThread.looper)
        handlerForOtherThread.post(Runnable {
            requestLocationUpdateByFLC()
            requestLocationUpdateByLM()
        })

        val handlerForMainThread = HandlerWithLooper(Looper.getMainLooper())
        handlerForMainThread.post{
            Thread.sleep(5000)
            Toast.makeText(applicationContext, "접근 알림!", Toast.LENGTH_LONG).show()
        }

        //오레오 버전 이상은 포그라운드 서비스(+고정 알림 포함) 설정을 해주어야 함.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            //Notification(상단 알림) 채널 생성
            val channel = NotificationChannel(
                ANDROID_CHANNEL_ID,
                "LocationService",
                NotificationManager.IMPORTANCE_NONE
            )

            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

            //Notification 객체를 가져옴
            notificationManager!!.createNotificationChannel(channel)

            //Notification 알림 객체 생성
            val builder = Notification.Builder(this, ANDROID_CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("SmartTracker")

            val notification = builder.build()

            //Notification 알림과 함께 포그라운드 서비스 시작
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.e("우진", "onStartCommand()")

        serviceIntent = intent

//        requestLocationUpdateByLM()
//        requestLocationUpdateByFLC()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationManager.removeUpdates(locationListener)
    }

    fun requestLocationUpdateByLM() {

        Log.e("우진", "현재 스레드: ${Thread.currentThread()}")

        val INTERVAL = 1000.toLong()
        val DISTANCE = 1.toFloat()

        locationListener = object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.e("우진",
                    "[업데이트된 위치 by LM] 위도: ${location.latitude} 경도: ${location.longitude}")
            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                Log.e("우진", "1")
            }

            override fun onProviderEnabled(p0: String?) {
                Log.e("우진", "2")
            }

            override fun onProviderDisabled(p0: String?) {
                Log.e("우진", "3")
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
            Log.e("우진", "권한 없음")
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

        Log.e("우진", "현재 스레드 flc!!!!: ${Thread.currentThread()}")

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
                        "[업데이트된 위치 by FLC] 위도: ${location.latitude} 경도: ${location.longitude}")

//                    lati.text = location.latitude.toString()
//                    longi.text = location.longitude.toString()

                    val application = application as CommonApplication
                    val queries = ArrayList<String>()
                    queries.add("다이소")
                    val temp = queries.toTypedArray()
                    application.apiContainer.storeListServiceRepository
                        .getToDoStoreListNearBy( 500,location.longitude.toFloat(), location.latitude.toFloat(), 100, success = {
                            Log.d("우진 다원", "Success Result $it")
                        }, fail = {
                            Log.d("우진 다원", "Fail Result $it")
                        }, queries = *temp)
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
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
}