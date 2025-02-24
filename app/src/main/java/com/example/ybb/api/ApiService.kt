package com.example.ybb.api

import retrofit2.http.GET
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

    @GET("/")
    suspend fun test(): String
}