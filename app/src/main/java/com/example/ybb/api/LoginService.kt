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
import com.example.ybb.utils.MMKVManager
import retrofit2.http.Body
import retrofit2.http.Query


/**
 *@author:郭炫烨
 *
 */
private val  apiService = RetrofitInstance.apiService
class LoginService {
    companion object{
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
        suspend fun makePlan(subject: Int, countByDay: Int):ResultVO<List<WordPlan>>{
            return apiService.makeWordPlan(PlanEntity(MMKVManager.getUserToken().toString(),subject,countByDay))
        }
        suspend fun getPlanList(
        ): ResultVO<List<Plan>>{
            return apiService.getPlanList(MMKVManager.getUserToken().toString())
        }
    }

}

class SubjectService{

    companion object{

        suspend fun getSubjects():ResultVO<List<Subject>>{
            return apiService.getAllSubjects()
        }
    }
}

object WordService{
    //1
    suspend fun getPlanList(
     subjectId: Int
    ): ResultVO<List<DateGroup>>{
        return apiService.getPlanList(MMKVManager.getUserToken().toString(),subjectId)
    }
    //2
    suspend fun getWordDate(
        dateGroupId: Int
    ): ResultVO<List<WordGroupDateGroup>>{
        return apiService.getWordDate(dateGroupId)
    }
    //3
    suspend fun getWord(
        subjectId: Int,
        wordGroupId: Int
    ): ResultVO<List<Word>>{
        return apiService.getWord(MMKVManager.getUserToken().toString(),subjectId,wordGroupId)
    }

    suspend fun completeList(
        wordGroupDateGroup: WordGroupDateGroup,
        isAllComplete: Boolean = false
    ): ResultVO<Boolean>{
        return apiService.completeList(wordGroupDateGroup,isAllComplete)
    }

    suspend fun getStudyPlan(
         subjectId: Int,
    ): ResultVO<List<WordGroupDateGroup>>{
        return apiService.getStudyPlan(subjectId,MMKVManager.getUserToken().toString())
    }

}
