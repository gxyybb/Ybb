package com.example.ybb.api

import com.example.ybb.api.LoginService.Companion.apiService
import com.example.ybb.api.entity.LoginRequest
import com.example.ybb.api.entity.LoginResponse
import com.example.ybb.api.entity.PlanEntity
import com.example.ybb.api.entity.WordPlan
import com.example.ybb.config.ResultVO


/**
 *@author:郭炫烨
 *
 */
class LoginService {
    companion object{
        private val  apiService = RetrofitInstance.apiService
        suspend fun login(userName: String, password:String):ResultVO<LoginResponse>{
            return  apiService.login(LoginRequest(userName,password))
        }
        suspend fun register(userName: String,password: String):ResultVO<LoginResponse>{
            return apiService.register(LoginRequest(userName,password))
        }


    }
}

class PlanService{
    companion object{
        private val  apiService = RetrofitInstance.apiService
        suspend fun makePlan(subject: String, countByDay: Int):ResultVO<List<WordPlan>>{
            return apiService.makeWordPlan(PlanEntity(subject,countByDay))
        }
    }

}