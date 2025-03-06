package com.example.ybb.api.entity


/**
 *@author:郭炫烨
 *
 */

data class LoginResponse(// Getter 和 Setter 方法
    var message: String, var token: String
) {
    // 重写toString()方法，方便调试时查看内容
    override fun toString(): String {
        return "LoginResponse{" +
                "message='" + message + '\'' +
                ", token='" + token + '\'' +
                '}'
    }
}
data class LoginRequest(// Getter 和 Setter 方法
    var username: String, var password: String
)

data class PlanEntity(val subject: String, val countByDay: Int)

data class WordPlan(val word:Word,val day:Int)

data class Word(
    val id: Long,                  // 单词ID
    val word: String,              // 单词本身
    val phonetic: String?,         // 音标（可为空）
    val definition: String,        // 释义
    val exampleSentence: String?,  // 例句（可为空）
    val audioUrl: String?,         // 发音音频 URL（可为空）
    val createdTime: Long          // 创建时间（时间戳）
)
