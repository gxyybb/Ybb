package com.example.ybb.api

import com.example.ybb.api.entity.LoginRequest
import com.example.ybb.api.entity.LoginResponse
import com.example.ybb.api.entity.PlanEntity
import com.example.ybb.api.entity.WordPlan
import com.example.ybb.config.ResultVO
import com.example.ybb.entity.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


/**
 *@author:郭炫烨
 *
 */
interface ApiService {

    // GET 请求：获取用户信息
    @GET("users/{userId}")
    suspend fun getUser(@Path("userId") userId: String): String

    // GET 请求：搜索用户
    @GET("/")
    suspend fun searchUsers(@Query("q") query: String): String

    @POST("user/login")
    suspend fun login(@Body loginRequest: LoginRequest) :ResultVO<LoginResponse>

    @POST("user/register")
    suspend fun register(@Body loginRequest: LoginRequest) :ResultVO<LoginResponse>

    @POST("plan/")
    suspend fun makeWordPlan(@Body planEntity: PlanEntity) : ResultVO<List<WordPlan>>

    //根据日期获取学习计划
    @GET("date/getWordGroupIdByDate")
    suspend fun getWordGroupIdByDate(@Query("token") token: String,@Query("date") date: Int,subjectId: Int)

    @GET("/word/ByGroup")
    suspend fun getWordListByGroup(@Query("id") groupNumber: Int,subjectId: Int)
}