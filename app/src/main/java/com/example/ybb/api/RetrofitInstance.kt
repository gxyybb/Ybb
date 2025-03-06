package com.example.ybb.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory


/**
 *@author:郭炫烨
 *
 */
object RetrofitInstance {

    private const val BASE_URL = "http://172.20.10.5:8085/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // 设置基础URL
            .addConverterFactory(GsonConverterFactory.create()) // 使用 Scalars 转换器
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java) // 创建接口的实现
    }
}