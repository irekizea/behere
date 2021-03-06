package com.behere.loc_based_reminder

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Delete

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo")
    fun getAll(): List<Todo>

    @Insert(onConflict = REPLACE)
    fun insert(todo: Todo)


    @Delete
    fun delete(todo : Todo)

}