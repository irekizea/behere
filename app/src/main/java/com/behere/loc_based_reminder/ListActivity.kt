package com.behere.loc_based_reminder

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_list.*


class ListActivity : AppCompatActivity() {
    private var todoDb : TodoDB? = null
    private var todoList = mutableListOf<Todo>()
    lateinit var mAdapter : TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        todoDb = TodoDB.getInstance(this)
        mAdapter = TodoAdapter(this, todoList)
        val itemTouchHelper = ItemTouchHelper(simpleItemTouchCallback)
        itemTouchHelper.attachToRecyclerView(mRecyclerView)

        val r = Runnable {
            try {
                todoList = (todoDb?.todoDao()?.getAll() as MutableList<Todo>?)!!
                mAdapter = TodoAdapter(this, todoList)
                mAdapter.notifyDataSetChanged()

                mRecyclerView.adapter = mAdapter
                mRecyclerView.layoutManager = LinearLayoutManager(this)
                mRecyclerView.setHasFixedSize(true)
            } catch (e: Exception) {
                Log.d("tag", "Error - $e")
            }
        }

        val thread = Thread(r)
        thread.start()

        mAddBtn.setOnClickListener {
            val intent = Intent(applicationContext, AddActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }



    override fun onStart() {
        super.onStart()
        for(temp in todoList){
            Log.e("우진 start", temp.doTodo)
        }
    }

    override fun onResume() {
        super.onResume()
        for(temp in todoList){
            Log.e("우진 resume", temp.doTodo)
        }
    }

    override fun onDestroy() {
        TodoDB.destroyInstance()
        todoDb = null
        super.onDestroy()
    }
    var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                @NonNull recyclerView: RecyclerView,
                @NonNull viewHolder: RecyclerView.ViewHolder,
                @NonNull target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(
                @NonNull viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {



                    val position = viewHolder.adapterPosition
                    val todo = todoList.removeAt(position)
                    mAdapter.notifyItemRemoved(position)
                        todoDb?.todoDao()?.delete(todo)




            }

        }

}
