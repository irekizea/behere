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
import org.json.JSONArray
import java.io.File

class MapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        intent.action?.let {
            if (it == FIND_ACTION) {
                val item = intent.getParcelableExtra<Item>("item") ?: return
                val i = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo: ${item.lat}, ${item.lon}?q=${item.lnoAdr} ")
                )
                startActivity(i)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.action?.let {
            if (it == FIND_ACTION) {
                val item = intent.getParcelableExtra<Item>("item") ?: return
                val i = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo: ${item.lat}, ${item.lon}?q=${item.lnoAdr}")
                )
                startActivity(i)
            }
        }
    }
}
