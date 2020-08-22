package com.behere.loc_based_reminder

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

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
    }

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeTv = itemView?.findViewById<TextView>(R.id.itemPlace)

        val todoTv = itemView?.findViewById<TextView>(R.id.itemTodo)

        fun bind(todo: Todo) {
            placeTv?.text = todo.doPlace

            todoTv?.text = todo.doTodo
        }
    }


}