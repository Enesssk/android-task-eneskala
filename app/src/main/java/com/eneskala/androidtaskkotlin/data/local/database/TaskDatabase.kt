package com.eneskala.androidtaskkotlin.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.eneskala.androidtaskkotlin.data.local.dao.TaskDao
import com.eneskala.androidtaskkotlin.data.model.Task

@Database(entities = [Task::class], version = 1)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}