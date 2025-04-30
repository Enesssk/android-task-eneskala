package com.eneskala.androidtaskkotlin.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.eneskala.androidtaskkotlin.data.model.Task

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks")
    fun getAll(): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<Task>)

    @Query("SELECT * FROM tasks WHERE " +
                "task LIKE '%'||:q||'%' OR " +
                "title LIKE '%'||:q||'%' OR " +
                "description LIKE '%'||:q||'%' OR " +
                "colorCode LIKE '%'||:q||'%'"
    )    fun search(q: String): LiveData<List<Task>>

    @Query("DELETE FROM tasks")
    suspend fun clearAll()

}