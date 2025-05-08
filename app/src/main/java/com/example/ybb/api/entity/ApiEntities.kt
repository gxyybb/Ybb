package com.example.ybb.api.entity

import java.io.Serializable


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

data class PlanEntity(val  token: String, val subject: Int, val countByDay: Int)

data class WordPlan(val word:Word,val day:Int)

data class Word(
    val id: Int? = null,
    val word: String? = null,
    val isNewWord: Int? = null,
    val isFamiliarWord: Int? = null,
    val pronunciation: String? = null,
    val meaning: String? = null,
    val wordType: String? = null,
    val audioPath: String? = null
) : Serializable

data class WordGroupDateGroup(
    val wordGroupId: Int? = null,
    val dateGroupId: Int? = null,
    val subjectId: Int? = null,
    val userId: Int? = null,
    val completionStatus: Int? = null
) : Serializable
data class DateGroup(
    val id: Int? = null,
    val date: Int? = null,
    val completionStatus: Int? = null,
    val userId: Int? = null,
    val subjectId: Int? = null
) : Serializable


data class Subject(
    val id: Int =0,  // 主键，可能为空
    val subjectName: String = ""
) : Serializable

data class Plan(
    val subject: Subject? = Subject(),
    val totalDays: Int = 0,
    val completeDays: Int = 0,
    val countByDay : Int = 0
)
