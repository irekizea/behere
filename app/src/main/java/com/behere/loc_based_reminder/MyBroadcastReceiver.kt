package com.behere.loc_based_reminder

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat

class MyBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when {
            intent?.action == Intent.ACTION_BOOT_COMPLETED -> {

                Log.e("우진", "Boot Complete 브로드캐스트 실행")

                context.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        //오레오 이상은 백그라운드로 실행하면 강제 종료 위험 있음 -> 포그라운드 실행해야
                        it?.startForegroundService(
                            Intent(
                                context?.applicationContext,
                                LocationUpdatingService::class.java
                            )
                        )
                        Log.e("우진", "API 레벨 26 이상")
                    } else {
                        //백그라운드 실행에 제약 없음
                        it?.startService(
                            Intent(
                                context?.applicationContext,
                                LocationUpdatingService::class.java
                            )
                        )
                        Log.e("우진", "API 레벨 25 이하")
//                    }
                    }
                }
            }
        }
    }
}