package com.behere.loc_based_reminder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add.*

class AddActivity : AppCompatActivity() {

    private var todoDb : TodoDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        todoDb = TodoDB.getInstance(this)

// 새로운 todo 객체를 생성, id 이외의 값을 지정 후 DB에 추가
        val addRunnable = Runnable {
            val newTodo = Todo()
            newTodo.doPlace = addPlace.text.toString()
            newTodo.doTodo = addTodo.text.toString()
            newTodo.doAlert = alertSwitch.isChecked
            todoDb?.todoDao()?.insert(newTodo)
            Log.e("주창",newTodo.doAlert.toString())
        }

        addBtn.setOnClickListener {
            val addThread = Thread(addRunnable)
            addThread.start()

            val i = Intent(this, ListActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }

        ic_return.setOnClickListener{

            val i = Intent(this, ListActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }
    }

    override fun onDestroy() {
        TodoDB.destroyInstance()
        super.onDestroy()
    }
}