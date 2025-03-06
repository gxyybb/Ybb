package com.example.ybb

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.example.ybb.config.AppDatabase
import com.example.ybb.dao.UserDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


fun <T> Class<T>.use(): T {
    return when (this) {
        UserDao::class.java -> AppDatabase.getDatabase(MyApplication.context).userDao() as T
        // 在这里你可以继续添加其他 DAO 类型
        else -> throw IllegalArgumentException("Unsupported DAO type")
    }
}

suspend fun showToast(text: String, time: Int = Toast.LENGTH_SHORT) {
    withContext(Dispatchers.Main) {
        Toast.makeText(MyApplication.context, text, time).show()
    }
}
