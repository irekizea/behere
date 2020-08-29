package com.behere.loc_based_reminder

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Todo::class], version = 2)
abstract class TodoDB: RoomDatabase() {
    abstract fun todoDao(): TodoDao

    companion object {
        private var INSTANCE: TodoDB? = null

        fun getInstance(context: Context): TodoDB? {
            if (INSTANCE == null) {
                synchronized(TodoDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        TodoDB::class.java, "todo.db")
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()

                }
            }
            return INSTANCE
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }
}