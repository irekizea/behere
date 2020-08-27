package com.behere.loc_based_reminder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
class Todo(@PrimaryKey var id: Long?,
           @ColumnInfo(name = "doplace") var doPlace: String?,
           @ColumnInfo(name = "doTodo") var doTodo: String?,
           @ColumnInfo(name = "doAlert") var doAlert: Boolean
){
    constructor(): this(null,"","",false)
}