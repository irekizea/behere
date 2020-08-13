package com.behere.loc_based_reminder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class WoojinTest: AppCompatActivity(){

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationListener: android.location.LocationListener

    private lateinit var locationManager:LocationManager

    private val PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION)

    private val REQUEST_CODE = 999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object: LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?:return

                for(location in locationResult.locations){
                    Log.e("우진", "[업데이트된 위치 by FLC] 위도: ${location.latitude} 경도: ${location.longitude}")
                }
            }
        }

        locationListener = object : android.location.LocationListener{
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
    }

    override fun onResume() {
        super.onResume()
        locationUpdateWithFLC()
        locationUpdateWithLM()

//        getLastLocationWithFLC()
//        getLastLocationWithLM()
    }

    override fun onPause() {
        super.onPause()

        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationManager.removeUpdates(locationListener)
    }

    //FusedLocationClient사용
    fun locationUpdateWithFLC(){

        val locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 3*1000
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
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_CODE)
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    //LocationManager 사용
    fun locationUpdateWithLM(){

        val INTERVAL = 3000.toLong()
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
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, DISTANCE, locationListener)
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, DISTANCE, locationListener)
    }

    fun getLastLocationWithFLC(){
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
        }

        fusedLocationClient.lastLocation.addOnSuccessListener {
                location: Location? ->
            Log.e("우진", "[getLastLocation by FLC]위도:${location?.latitude} 경도:${location?.longitude}")
        }
    }

    fun getLastLocationWithLM(){

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
        }

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        Log.e("우진", "[getLastLocation by LM]위도:${location?.latitude} 경도:${location?.longitude}")
    }
}