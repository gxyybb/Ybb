package com.example.ybb.api

import com.example.ybb.api.entity.DateGroup
import com.example.ybb.api.entity.LoginRequest
import com.example.ybb.api.entity.LoginResponse
import com.example.ybb.api.entity.Plan
import com.example.ybb.api.entity.PlanEntity
import com.example.ybb.api.entity.Subject
import com.example.ybb.api.entity.Word
import com.example.ybb.api.entity.WordGroupDateGroup
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

    @POST("word/generateStudyPlan")
    suspend fun makeWordPlan(@Body planEntity: PlanEntity) : ResultVO<List<WordPlan>>

    //根据日期获取学习计划
    @GET("date/getWordGroupIdByDate")
    suspend fun getWordGroupIdByDate(@Query("token") token: String,@Query("date") date: Int,subjectId: Int)

    @GET("/word/ByGroup")
    suspend fun getWordListByGroup(@Query("id") groupNumber: Int,subjectId: Int)
    @GET("subject")  // 确保这里的路径与后端对应
    suspend fun getAllSubjects(): ResultVO<List<Subject>>
    @GET("/word/getDateGroupIdByUser")
    suspend fun getPlanList(
        @Query("token") token: String,
        @Query("subjectId") subjectId: Int
    ): ResultVO<List<DateGroup>>

    // 根据日期组 ID 获取单词分组（WordGroupDateGroup）
    @GET("/word/getWordDateByDateGroupId")
    suspend fun getWordDate(
        @Query("dateGroupId") dateGroupId: Int
    ): ResultVO<List<WordGroupDateGroup>>

    // 根据分组 ID 获取用户的单词列表
    @GET("/word/getWordByWordDate")
    suspend fun getWord(
        @Query("token") token: String,
        @Query("subjectId") subjectId: Int,
        @Query("wordGroupId") wordGroupId: Int
    ): ResultVO<List<Word>>

    @GET("/word/getPlanList")
    suspend fun getPlanList(
        @Query("token") token: String
    ): ResultVO<List<Plan>>
    @POST("/word/completeList")
    suspend fun completeList(
        @Body wordGroupDateGroup: WordGroupDateGroup,
        @Query("isAllComplete") isAllComplete: Boolean ,
    ): ResultVO<Boolean>
    @GET("/word/studyPlan")
    suspend fun getStudyPlan(
        @Query("subjectId") subjectId: Int,
        @Query("token") token: String
    ): ResultVO<List<WordGroupDateGroup>>
}