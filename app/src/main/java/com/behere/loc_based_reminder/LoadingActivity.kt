package com.behere.loc_based_reminder

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.behere.loc_based_reminder.MainActivity
import com.behere.loc_based_reminder.R

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val handler = Handler()
        handler.postDelayed(
            Runnable {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            },
            1500
        )
    }
}