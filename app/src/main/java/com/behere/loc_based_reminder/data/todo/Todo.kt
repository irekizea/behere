package com.behere.loc_based_reminder.data.todo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class Todo(@PrimaryKey var id: Long?,
           @ColumnInfo(name = "doplace") var doPlace: String?,
           @ColumnInfo(name = "doTodo") var doTodo: String
){
    constructor(): this(null,"","")
}