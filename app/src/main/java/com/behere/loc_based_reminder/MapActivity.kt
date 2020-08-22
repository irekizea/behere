package com.behere.loc_based_reminder

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
                val file = File(applicationContext.filesDir, FILE_NAME)
                if (file.exists()) {
                    val arr = JSONArray(readFile(file))
                    if (arr.length() < 1) {
                        return
                    }
                    val obj = arr.getJSONObject(0)
                    val item = Item(obj.getString("adongCd"),
                        obj.getString("adongNm"),
                        obj.getString("bizesId"),
                        obj.getString("bizesNm"),
                        obj.getString("bldMngNo"),
                        obj.getString("bldMnno"),
                        obj.getString("bldNm"),
                        obj.getString("bldSlno"),
                        obj.getString("brchNm"),
                        obj.getString("ctprvnCd"),
                        obj.getString("ctprvnNm"),
                        obj.getString("dongNo"),
                        obj.getString("flrNo"),
                        obj.getString("hoNo"),
                        obj.getString("indsLclsCd"),
                        obj.getString("indsLclsNm"),
                        obj.getString("indsMclsCd"),
                        obj.getString("indsMclsNm"),
                        obj.getString("indsSclsCd"),
                        obj.getString("indsSclsNm"),
                        obj.getString("ksicCd"),
                        obj.getString("ksicNm"),
                        obj.getString("lat"),
                        obj.getString("ldongCd"),
                        obj.getString("ldongNm"),
                        obj.getString("lnoAdr"),
                        obj.getString("lnoCd"),
                        obj.getString("lnoMnno"),
                        obj.getString("lnoSlno"),
                        obj.getString("lon"),
                        obj.getString("newZipcd"),
                        obj.getString("oldZipcd"),
                        obj.getString("plotSctCd"),
                        obj.getString("plotSctNm"),
                        obj.getString("rdnm"),
                        obj.getString("rdnmAdr"),
                        obj.getString("rdnmCd"),
                        obj.getString("signguCd"),
                        obj.getString("signguNm")
                        )

                    val i = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("geo: ${item.lat}, ${item.lon}?q=${item.lnoAdr}")
                    )
                    startActivity(i)
                }
            }
        }
    }
}