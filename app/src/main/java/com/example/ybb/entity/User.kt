package com.example.ybb.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // 自动生成的主键
    @ColumnInfo(name = "name") val name: String,       // 映射到表的列
    @ColumnInfo(name = "age") val age: Int,            // 映射到表的列
    @ColumnInfo(defaultValue = "")val description: String
)
