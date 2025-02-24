package com.example.ybb.api

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory


/**
 *@author:郭炫烨
 *
 */object RetrofitInstance {

    private const val BASE_URL = "http://www.baidu.com/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // 设置基础URL
            .addConverterFactory(ScalarsConverterFactory.create()) // 使用 Scalars 转换器
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java) // 创建接口的实现
    }
}