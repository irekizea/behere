package com.behere.loc_based_reminder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationListener: android.location.LocationListener
    private lateinit var locationManager: LocationManager

    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private val REQUEST_CODE = 9999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

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

            Log.e(
                "우진",
                "위치 권한 없음"
            )

            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
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
                }
            }
        }

        locationListener = object : android.location.LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.e("우진", "[업데이트된 위치 by LM] 위도: ${location.latitude} 경도: ${location.longitude}")
            }

            override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
                TODO("Not yet implemented")
            }

            override fun onProviderEnabled(p0: String?) {
                TODO("Not yet implemented")
            }

            override fun onProviderDisabled(p0: String?) {
                TODO("Not yet implemented")
            }
        }

        val intent = Intent(this, LocationUpdatingService::class.java)

        //서비스 시작, 중지 트리거
        start.setOnClickListener {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //오레오 이상은 백그라운드로 실행하면 강제 종료 위험 있음 -> 포그라운드 실행해야
                startForegroundService(Intent(applicationContext, LocationUpdatingService::class.java))
                Log.e("우진", "API 레벨 26 이상")
            } else {
                //백그라운드 실행에 제약 없음
                startService(Intent(applicationContext, LocationUpdatingService::class.java))
                Log.e("우진", "API 레벨 25 이하")
            }

            Log.e("우진", "서비스 시작")
        }

        stop.setOnClickListener {
            stopService(intent)
            Log.e("우진", "서비스 중지")
        }

//        locationUpdateWithFLC()
//        locationUpdateWithLM()

//        getLastLocationWithFLC()
//        getLastLocationWithLM()
    }

    //FusedLocationProviderClient 사용
    fun locationUpdateWithFLC() {

        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//            interval = 5 * 1000
            fastestInterval = 1 * 1000
            smallestDisplacement = 1.toFloat() //기본: 50미터
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
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
        } else {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    fun getLastLocationWithFLC() {
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
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
        } else {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                Log.e(
                    "우진",
                    "[마지막 위치 by FLC]위도:${location?.latitude} 경도:${location?.longitude}"
                )
            }
        }
    }

    //LocationManager 사용
    fun locationUpdateWithLM() {

        val INTERVAL = 1000.toLong()
        val DISTANCE = 1.toFloat()

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
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
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

    fun getLastLocationWithLM() {

        var location: Location? = null

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
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
        } else {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            Log.e("우진", "[마지막 위치 by LM]위도:${location?.latitude} 경도:${location?.longitude}")
        }
    }
}



