package com.example.ybb.dao


import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.ybb.MyApplication
import com.example.ybb.config.AppDatabase
import com.example.ybb.entity.User

@Dao
interface UserDao:BaseDao<User>{

    // 查询所有用户
    @Query("SELECT * FROM user_table")
    fun getAllUsers(): List<User>
}

