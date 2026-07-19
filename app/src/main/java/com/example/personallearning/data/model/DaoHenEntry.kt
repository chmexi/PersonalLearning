package com.example.personallearning.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daohen_entries",
    indices = [Index(value = ["date"], unique = true)]
)
data class DaoHenEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,         // "2026-06-29"
    val q1: String = "",      // 最起波澜的一件事
    val q2: String = "",      // 第一反应
    val q3: String = "",      // 其实想得到什么
    val q4: String = "",      // 其实在害怕什么
    val q5: String = "",      // 自己找了什么理由
    val q6: String = "",      // 主石头
    val q7: String = "",      // 明天怎么做
    val syncStatus: Int = 0,  // 0=本地有修改, 1=与云端一致
    val serverRevision: Int = 0 // 本地最后一次看到的云端版本，0=云端不存在/未知
)
