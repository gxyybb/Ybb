package com.example.ybb

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ybb.api.LoginService
import com.example.ybb.api.PlanService
import com.example.ybb.api.entity.WordPlan
import com.example.ybb.dao.UserDao
import com.example.ybb.entity.User
import com.example.ybb.utils.MMKVManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject


/**
 *@author:郭炫烨
 *
 */

@HiltViewModel
class MainViewModel @Inject constructor(
) : ViewModel() {
    private val _isLogin = MutableStateFlow(MMKVManager.getLoginState()) // 初始值
    val isLogin: StateFlow<Boolean> = _isLogin.asStateFlow()
    val errorMsg = MutableStateFlow("")
    val studyPlanState = MutableStateFlow(StudyPlanState())
    val planList = MutableStateFlow(emptyList<WordPlan>())

    private val userDao = UserDao::class.java.use()

    fun onSelectedSubject(subject:String){
        viewModelScope.launch {
            studyPlanState.emit(studyPlanState.value.copy(subject = subject))
        }
    }

    fun onSelectedConfirm(){
        viewModelScope.launch {
            studyPlanState.emit( studyPlanState.value.copy(
                state = InitWordState.LOADING
            ))
            try {
                withTimeout(10_000) { // 设置超时时间为10秒
                    withContext(Dispatchers.IO) {
                        studyPlanState.value.subject?.let { subject ->
                            studyPlanState.value.countInDay?.let { count ->
                                val plan = PlanService.makePlan(subject, count)
                                MMKVManager.setWordPlan(true)
                                planList.emit(plan.data)
                                studyPlanState.value = studyPlanState.value.copy(state = InitWordState.COMPLETE) // 成功

                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                studyPlanState.value = studyPlanState.value.copy(state = InitWordState.ERROR) // 超时
            } catch (e: Exception) {
                studyPlanState.value = studyPlanState.value.copy(state = InitWordState.ERROR) // 其他异常
            }
        }

    }

    fun onSelectedCount(count:Int){
        viewModelScope.launch {
            studyPlanState.emit(studyPlanState.value.copy(countInDay = count))
        }
    }

    fun login(userName:String, password: String){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val loginVo = LoginService.login(userName,password)
                if (loginVo.isSuccess){
                    MMKVManager.setLoginState(true)
                    showToast("登录成功")
                    _isLogin.emit(true)
                }else{
                    errorMsg.emit(loginVo.msg)
                    Log.i(TAG,loginVo.msg)
                }
            }
        }
    }




    // 增加用户
    fun addUser() {
        val user = User(
            name = "124", age = 12,
            description = "1"
        )
        viewModelScope.launch {
            try {
                // 在后台线程插入数据
                withContext(Dispatchers.IO) {
                    userDao.insert(user)
                }
                Log.d("MainViewModel", "User added successfully")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Error inserting user", e)
            }
        }
    }

    // 获取所有用户
    fun getAll() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    val users = userDao.getAllUsers()

                    Log.d("MainViewModel", "Users fetched: ${users}")
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching users", e)
            }
        }
    }
}

