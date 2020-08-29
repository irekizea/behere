package com.behere.loc_based_reminder

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.behere.loc_based_reminder.data.todo.Todo
import kotlinx.android.synthetic.main.activity_add.*
import kotlinx.android.synthetic.main.activity_main.*

class AddActivity : AppCompatActivity() {

    private var todoDb: TodoDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        todoDb = TodoDB.getInstance(this)

        // 새로운 todo 객체를 생성, id 이외의 값을 지정 후 DB에 추가

        save_btn.setOnClickListener {
            val newTodo = Todo()
            newTodo.doPlace = location_edit.text.toString()
            newTodo.doTodo = todo_edit.text.toString()
            newTodo.doAlert = alertSwitch.isChecked
            application.apiContainer.todoDao.insert(newTodo)
            Log.e("주창",newTodo.doAlert.toString())


            val i = Intent(this, MainActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }

        back_btn.setOnClickListener{

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