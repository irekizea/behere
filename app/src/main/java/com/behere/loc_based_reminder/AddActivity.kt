package com.behere.loc_based_reminder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
            todoDb?.todoDao()?.insert(newTodo)


        }

        addBtn.setOnClickListener {
            val addThread = Thread(addRunnable)
            addThread.start()

            val i = Intent(this, ListActivity::class.java)
            startActivity(i)

            finish()

        }

        ic_return.setOnClickListener{
            finish()
        }
    }

    override fun onDestroy() {
        TodoDB.destroyInstance()
        super.onDestroy()
    }
}