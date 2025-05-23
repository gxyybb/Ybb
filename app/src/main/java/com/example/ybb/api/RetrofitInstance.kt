package com.example.ybb.api


/**
 *@author:郭炫烨
 *
 */
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BASE_URL = "http://172.20.10.6:8087/"

    // 定义 OkHttp 拦截器，用于打印请求和响应信息
    private val loggingInterceptor = Interceptor { chain ->
        val request = chain.request()

        // 打印请求信息
        Log.i("[HTTP]","${request.url()}")


        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: IOException) {
            println("请求失败: ${e.localizedMessage}")
            throw e
        }

        // 打印响应信息
        val responseBody = response.peekBody(Long.MAX_VALUE)  // 获取响应体内容但不消耗流

        Log.i("[HTTP]","$responseBody")

        return@Interceptor response
    }

    // 创建 OkHttpClient 并添加拦截器
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    // 配置 Retrofit，使用添加了拦截器的 OkHttpClient
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // 设置基础URL
            .client(okHttpClient) // 使用 OkHttpClient
            .addConverterFactory(GsonConverterFactory.create()) // 使用 Gson 转换器
            .build()
    }

    // 创建 ApiService 实例
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java) // 创建接口的实现
    }
}
