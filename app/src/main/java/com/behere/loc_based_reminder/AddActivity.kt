package com.behere.loc_based_reminder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.behere.loc_based_reminder.service.LocationUpdatingService
import kotlinx.android.synthetic.main.activity_add.*

const val tag = "TODO"

class AddActivity : AppCompatActivity() {

    private var todoDb: TodoDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        todoDb = TodoDB.getInstance(this)

        // 새로운 todo 객체를 생성, id 이외의 값을 지정 후 DB에 추가
        val application = application as CommonApplication
        save_btn.setOnClickListener {
            val newTodo = Todo()
            newTodo.doPlace = location_edit.text.toString()
            newTodo.doTodo = todo_edit.text.toString()
            newTodo.doAlert = alarm_switch.isChecked
            application.apiContainer.todoDao.insert(newTodo)
            Log.e(tag, newTodo.doAlert.toString())

            if (LocationUpdatingService.serviceIntent == null) {
                val startServiceIntent = Intent(this, LocationUpdatingService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(startServiceIntent)
                } else {
                    startService(startServiceIntent)
                }
                Log.e(TAG, "Start service")
            } else {
                //No service, No schedule
            }

            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }

        back_btn.setOnClickListener {
            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }
    }

    override fun onDestroy() {
        TodoDB.destroyInstance()
        super.onDestroy()
    }
}