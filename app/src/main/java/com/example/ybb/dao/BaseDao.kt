package com.example.ybb.dao




import androidx.lifecycle.LiveData
import androidx.room.*

// BaseDao接口，定义通用的增删改查操作
interface BaseDao<T> {

    @Insert
    suspend fun insert(item: T)

    @Update
    suspend fun update(item: T)

    @Delete
    suspend fun delete(item: T)

}
