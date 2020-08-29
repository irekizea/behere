package com.behere.loc_based_reminder

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.behere.loc_based_reminder.data.response.Item
import com.behere.loc_based_reminder.service.FILE_NAME
import com.behere.loc_based_reminder.service.FIND_ACTION
import com.behere.loc_based_reminder.util.readFile
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import java.io.File

class MapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    lateinit var map: GoogleMap
    lateinit var result: List<Item>
    var lat: Double = 37.56
    var lng: Double = 126.97


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        intent.action?.let {
            if (it == FIND_ACTION) {
                result = intent.getParcelableArrayListExtra<Item>("items") ?: return
                lat = intent.getDoubleExtra("lat", 37.56)
                lng = intent.getDoubleExtra("lng", 126.97)
                drawMarker()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.action?.let {
            result = intent.getParcelableArrayListExtra<Item>("items") ?: return
            drawMarker()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap == null) return
        map = googleMap
        if (this::result.isInitialized) {
            map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(lat, lng)))
            map.animateCamera(CameraUpdateFactory.zoomTo(15F))
            map.setOnMarkerClickListener(this)
            drawMarker()
        }
    }

    private fun drawMarker() {
        for (item in result) {
            val markerOption = MarkerOptions()
            val latlang = LatLng(item.lat.toDouble(), item.lon.toDouble())
            markerOption.position(latlang)
            markerOption.title(item.bizesNm)
            if (this::map.isInitialized) {
                map.addMarker(markerOption)
            }
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        if (p0 == null) return false
        val i = Intent(
            Intent.ACTION_VIEW,
            //geo:0,0?q=34.99,-106.61(Treasure)"
            Uri.parse("geo:0,0?q=${p0.position.latitude}, ${p0.position.longitude}(${p0.title})")
        )
        startActivity(i)
        return false
    }

}
