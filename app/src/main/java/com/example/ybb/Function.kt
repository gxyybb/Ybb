package com.example.ybb

import com.example.ybb.config.AppDatabase
import com.example.ybb.dao.UserDao



fun <T> Class<T>.use(): T {
    return when (this) {
        UserDao::class.java -> AppDatabase.getDatabase(MyApplication.context).userDao() as T
        // 在这里你可以继续添加其他 DAO 类型
        else -> throw IllegalArgumentException("Unsupported DAO type")
    }
}