package com.example.personallearning.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "express_entries")
data class ExpressEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val retelling: String = "",
    val keywords: String = "",
    val observation: String = "",
    val listening: String = "",
    val compression: String = "",
    val duration: Int = 0,
    val syncStatus: Int = 0
)
