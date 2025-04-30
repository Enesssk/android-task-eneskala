package com.eneskala.androidtaskkotlin.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    @SerializedName("task")
    val task: String,
    val title: String,
    val description: String,
    val sort: String,
    val wageType: String,
    val BusinessUnitKey: String?,
    val businessUnit: String,
    val parentTaskID: String?,
    val preplanningBoardQuickSelect: String?,
    val colorCode: String?,
    val workingTime: String?,
    val isAvailableInTimeTrackingKioskMode: Boolean,
    val isAbstract: Boolean
) : Serializable  // I made it serializable to pull the data to the detail screen.

