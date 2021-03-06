package com.behere.loc_based_reminder

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_todo.view.*

class TodoAdapter(val context: Context, val todos: List<Todo>) :
    RecyclerView.Adapter<TodoAdapter.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_todo, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder?.bind(todos[position])

        holder.check_box.setOnClickListener {
            if (!it.isSelected) {
                holder.todo_content.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                it.isSelected = true
            } else {
                holder.todo_content.paintFlags = Paint.ANTI_ALIAS_FLAG
                it.isSelected = false
            }
        }
    }

    //    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val placeTv = itemView?.findViewById<TextView>(R.id.itemPlace)
//
//        val todoTv = itemView?.findViewById<TextView>(R.id.itemTodo)
//
//        fun bind(todo: Todo) {
//            placeTv?.text = todo.doPlace
//
//            todoTv?.text = todo.doTodo
//        }
//    }
    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val todo_content = itemView.todo_content
        val check_box = itemView.check_box

        fun bind(todo: Todo) {
            todo_content?.text = todo.doTodo
        }
    }
}