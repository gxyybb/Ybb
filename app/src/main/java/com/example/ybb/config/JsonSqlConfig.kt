package com.example.ybb.config


import android.content.Context
import com.example.ybb.R
import org.json.JSONObject
import java.io.InputStream


class MigrationLoader(private val context: Context) {

    // 存储迁移脚本的缓存
    private var cachedMigrations: Map<String, String>? = null

    // 读取并解析 JSON 文件，支持缓存
    fun loadMigrations(): Map<String, String> {
        // 如果缓存已加载，直接返回缓存内容
        cachedMigrations?.let {
            return it
        }

        val migrations = mutableMapOf<String, String>()
        try {
            // 从 res/raw 目录中读取 JSON 文件
            val inputStream: InputStream = context.resources.openRawResource(R.raw.migrations)
            val jsonString = inputStream.bufferedReader().use { it.readText() }

            // 解析 JSON
            val jsonObject = JSONObject(jsonString)
            jsonObject.keys().forEach { key ->
                migrations[key] = jsonObject.getString(key)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 缓存迁移脚本
        cachedMigrations = migrations
        return migrations
    }
}
