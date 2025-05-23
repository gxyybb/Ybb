package com.example.ybb

import android.util.Log
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ybb.api.ChatService
import com.example.ybb.api.LoginService
import com.example.ybb.api.PlanService
import com.example.ybb.api.SubjectService
import com.example.ybb.api.WordService
import com.example.ybb.api.entity.DateGroup
import com.example.ybb.api.entity.Plan
import com.example.ybb.api.entity.Subject
import com.example.ybb.api.entity.Word
import com.example.ybb.api.entity.WordGroupDateGroup
import com.example.ybb.dao.UserDao
import com.example.ybb.entity.User
import com.example.ybb.utils.MMKVManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject


/**
 *@author:郭炫烨
 *
 */
sealed class LoadState {
    object Idle : LoadState()
    object Loading : LoadState()
    object Success : LoadState()
    data class Error(val message: String?) : LoadState()
}

data class BarUiState(
    val subjectId: Int = 0,
    val plan: Plan = Plan(),
    val dateGroupId: Int = 0,
    val day: Int = 0,
    val wordGroupNumber: Int = 0,
    val totalDays: Int = 0,
    val completeDays: Int = 0,
    val countByDay: Int = 0,
    val bottomIndex: Int = 0,
    val bottomItem: WordGroupDateGroup? = WordGroupDateGroup(),
    val isDayComplete: Boolean = false,
)

data class ChatData(val msg: String,val isMe: Boolean)

@HiltViewModel
class MainViewModel @Inject constructor(
) : ViewModel() {
    private val _isLogin = MutableStateFlow(MMKVManager.getLoginState()) // 初始值
    val isLogin: StateFlow<Boolean> = _isLogin.asStateFlow()
    val errorMsg = MutableStateFlow("")
    val studyPlanState = MutableStateFlow(StudyPlanState())
    private val _planList = MutableStateFlow<List<Plan>>(emptyList())
    val planList: StateFlow<List<Plan>> = _planList
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading
    val subjects = MutableStateFlow(emptyList<Subject>())

    val topBarInfo = MutableStateFlow(emptyList<DateGroup>())
    val bottomBarInfo = MutableStateFlow(emptyList<WordGroupDateGroup>())
    val centerInfo = MutableStateFlow(emptyList<Word>())

    private val _barLoadState = MutableStateFlow<LoadState>(LoadState.Idle)
    private val _barUiState = MutableStateFlow(BarUiState())
    val barUiState: StateFlow<BarUiState> = _barUiState
    val barLoadState: StateFlow<LoadState> = _barLoadState
    private val _studyForm = MutableStateFlow(emptyList<WordGroupDateGroup>())
    val studyForm: StateFlow<List<WordGroupDateGroup>> = _studyForm

    private val _chatList = MutableStateFlow<List<ChatData>>(emptyList())
    val chatList: StateFlow<List<ChatData>> = _chatList


    private var barLoadJob: Job? = null
    private var changeDayJob: Job? = null
    private val userDao = UserDao::class.java.use()

    fun initBarUiState(plan: Plan) {
        viewModelScope.launch {
            _barUiState.emit(
                barUiState.value.copy(
                    totalDays = plan.totalDays,
                    completeDays = plan.completeDays,
                    countByDay = plan.countByDay,
                    plan = plan
                )
            )
        }

    }
    private val chatMutex = Mutex()

    fun chat(msg: String) {
        viewModelScope.launch {
            chatMutex.withLock {
                // 先更新UI，把我的消息添加进去（主线程操作）
                withContext(Dispatchers.Main) {
                    _chatList.update { oldList -> oldList + ChatData(msg, isMe = true) }
                }

                // 调用网络请求（IO线程）
                val chat = withContext(Dispatchers.IO) {
                    ChatService.chat(msg)
                }

                // 更新AI回复（主线程）
                withContext(Dispatchers.Main) {
                    if (chat.code == 200) {
                        chat.msg?.let { reply ->
                            _chatList.update { oldList -> oldList + ChatData(reply, isMe = false) }
                        }
                    } else {
                        _chatList.update { oldList -> oldList + ChatData("请求失败: ${chat.msg}", isMe = false) }
                    }
                }
            }
            Log.i(TAG,"_chatList:${_chatList.value.toString()}")
        }
    }




    fun getStudyForm() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = WordService.getStudyPlan(barUiState.value.subjectId)
                if (result.code == 200 && result.data != null) {
                    _studyForm.emit(result.data)
                } else {
                    // 可根据 result.code 显示错误提示
                    Log.e("getStudyForm", "接口返回失败：${result.msg}")
                }
            } catch (e: Exception) {
                Log.e("getStudyForm", "网络异常", e)
                // 可以用 StateFlow 或 callback 显示错误UI
            }
        }
    }


    fun completeList(wordGroupDateGroup: WordGroupDateGroup) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.i(TAG, "completeList: $wordGroupDateGroup")

            val bottomBar = bottomBarInfo.value


            val hasOtherIncomplete = bottomBar.any {
                it.wordGroupId != wordGroupDateGroup.wordGroupId &&
                        it.completionStatus == 0
            }

            // 发起请求
            val result = WordService.completeList(wordGroupDateGroup, !hasOtherIncomplete)
            Log.i(TAG, "completeList: isCompleteDay=${!hasOtherIncomplete}, isOK=${result.data}")

            if (!result.data) return@launch

            // 更新 bottomBar 对应项
            val bottomIndex = bottomBar.indexOfFirst {
                it.wordGroupId == wordGroupDateGroup.wordGroupId
            }

            if (bottomIndex != -1) {
                val updatedBottomBar = bottomBar.toMutableList().apply {
                    this[bottomIndex] = this[bottomIndex].copy(completionStatus = 1)
                }
                bottomBarInfo.emit(updatedBottomBar)
                Log.i(TAG, "completeList: updated bottomBarInfo = $updatedBottomBar")
            }

            // 更新 barUiState

            Log.i(TAG, "completeList: updated barUiState = ${_barUiState.value}")

            // 如果这天所有都完成，更新 topBar
            if (!hasOtherIncomplete) {
                val topBar = topBarInfo.value
                val topIndex = topBar.indexOfFirst { it.id == wordGroupDateGroup.dateGroupId }

                if (topIndex != -1) {
                    val updatedTopBar = topBar.toMutableList().apply {
                        this[topIndex] = this[topIndex].copy(completionStatus = 1)
                    }
                    topBarInfo.emit(updatedTopBar)

                    Log.i(TAG, "completeList: updated topBarInfo = $updatedTopBar")
                }
            }
            _barUiState.emit(
                barUiState.value.copy(
                    bottomItem = wordGroupDateGroup.copy(completionStatus = 1),
                    isDayComplete = !hasOtherIncomplete
                )
            )
        }
    }

    fun onSelectedSubject(subject: Int) {
        viewModelScope.launch {
            studyPlanState.emit(studyPlanState.value.copy(subject = subject))
            initBarInfo(subject)
        }
    }

    fun loadPlans() {
        viewModelScope.launch {
            // 假设这是从网络请求获取的逻辑
            val result = PlanService.getPlanList()
            if (result.code == 200) {
                _planList.value = result.data ?: emptyList()
            }
            _isLoading.value = false
        }
    }

    fun getTopBarInfo(subjectId: Int): Deferred<List<DateGroup>> {
        Log.i(TAG, "getTopBarInfo$subjectId")
        return viewModelScope.async {
            try {
                val planVO = withContext(Dispatchers.IO) {
                    WordService.getPlanList(subjectId)
                }
                topBarInfo.emit(planVO.data)
                Log.i(TAG, "getTopBarInfo: ${planVO}")
                planVO.data
            } catch (e: Exception) {
                Log.e("ViewModel", "getTopBarInfo 请求失败: ${e.message}")
                emptyList()
            }
        }
    }

    fun getBottomBarInfoAsync(dateGroupId: Int): Deferred<List<WordGroupDateGroup>> {
        return viewModelScope.async {
            try {
                val wordDateVo = withContext(Dispatchers.IO) {
                    WordService.getWordDate(dateGroupId = dateGroupId)
                }
                Log.i(TAG, "getBottomBarInfo: ${wordDateVo}")
                bottomBarInfo.emit(wordDateVo.data)
                wordDateVo.data
            } catch (e: Exception) {
                Log.e("ViewModel", "getBottomBarInfo 请求失败: ${e.message}")
                emptyList()
            }
        }
    }

    fun getCenterBarInfoAsync(subjectId: Int, wordGroupId: Int): Deferred<List<Word>> {
        return viewModelScope.async {
            try {
                val wordVo = withContext(Dispatchers.IO) {
                    WordService.getWord(subjectId, wordGroupId)
                }
                Log.i(TAG, "getCenterBarInfo: ${wordVo}")
                centerInfo.emit(wordVo.data)
                wordVo.data
            } catch (e: Exception) {
                Log.e("ViewModel", "getCenterBarInfo 请求失败: ${e.message}")
                emptyList()
            }
        }
    }

    fun dayClick(day: Int) {
        changeDayJob?.cancel()
        changeDayJob = viewModelScope.launch(Dispatchers.IO) {
            topBarInfo.value.firstOrNull { it.date == day }?.let { plan ->
                _barUiState.emit(
                    barUiState.value.copy(
                        bottomIndex = 0,
                        isDayComplete = plan.completionStatus == 1
                    )
                )
                loadBarInfo(plan)
            } ?: Log.i(TAG, "dayClick: date group not found for day = $day")
        }
    }

    fun initBarInfo(subjectId: Int) {
        barLoadJob?.cancel()
        barLoadJob = viewModelScope.launch(Dispatchers.IO) {
            _barLoadState.emit(LoadState.Loading)
            try {
                val topList = getTopBarInfo(subjectId).await()
                val first = topList.firstOrNull { it.completionStatus == 0 }
                if (first != null) {
                    loadBarInfo(first, subjectId)
                    _barLoadState.emit(LoadState.Success)
                } else {
                    Log.i(TAG, "initBarInfo: No unfinished date group found")
                    _barLoadState.emit(LoadState.Success) // 仍然视为成功，但无任务
                }
            } catch (e: Exception) {
                _barLoadState.emit(LoadState.Error(e.message))
            }
        }
    }

    private suspend fun loadBarInfo(plan: DateGroup, subjectId: Int = barUiState.value.subjectId) {
        val planId = plan.id
        val planDate = plan.date

        if (planId == null || planDate == null) {
            Log.i(TAG, "loadBarInfo: 学习计划 ID 或日期为空")
            return
        }

        val bottomList = getBottomBarInfoAsync(planId).await()
        val bottomIndex = 0
        val bottomInfo = bottomList.getOrNull(bottomIndex)

        var bar = barUiState.value.copy(
            subjectId = subjectId,
            dateGroupId = planId,
            day = planDate,
            bottomIndex = bottomIndex
        )

        bar = updateBarWithBottomInfo(bar, bottomInfo, subjectId)

        _barUiState.emit(bar)
        Log.i(TAG, "loadBarInfo result: $bar")
    }

    fun onChangeBottom(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val bottomInfo = bottomBarInfo.value.getOrNull(index)
            var bar = barUiState.value.copy(bottomIndex = index)

            bar = updateBarWithBottomInfo(bar, bottomInfo)
            _barUiState.emit(bar)
        }
    }

    private suspend fun updateBarWithBottomInfo(
        bar: BarUiState,
        bottomInfo: WordGroupDateGroup?,
        subjectId: Int = bar.subjectId
    ): BarUiState {
        var newBar = bar.copy(
            wordGroupNumber = bottomInfo?.wordGroupId ?: 0,
            bottomItem = bottomInfo
        )

        if (bottomInfo?.wordGroupId != null) {
            getCenterBarInfoAsync(subjectId, bottomInfo.wordGroupId).await()
        } else {
            Log.i(TAG, "updateBarWithBottomInfo: 无效的或缺失的词组 ID")
        }
        return newBar
    }


    fun getSubjects() {
        viewModelScope.launch(Dispatchers.IO) {
            val subjectsVo = SubjectService.getSubjects()
            val subject = subjectsVo.data
            Log.i(TAG, "$subject")
            withContext(Dispatchers.Main) {
                subjects.emit(subject)
            }

        }
    }


    fun onSelectedConfirm() {
        viewModelScope.launch {
            studyPlanState.emit(
                studyPlanState.value.copy(
                    state = InitWordState.LOADING
                )
            )
            try {
                withTimeout(10_000) { // 设置超时时间为10秒
                    withContext(Dispatchers.IO) {
                        studyPlanState.value.subject?.let { subject ->
                            studyPlanState.value.countInDay?.let { count ->
                                Log.i(
                                    TAG,
                                    "onSelectedConfirm:studyPlanState.value.subject:$subject，count:$count"
                                )

                                val plan = PlanService.makePlan(subject, count)
                                loadPlans()
                                Log.i(TAG, "planCode:${plan.code}")
                                if (plan.code == 200) {
                                    MMKVManager.setWordPlan(true)
                                    MMKVManager.setWordFirstUsed(false)

                                    studyPlanState.emit(studyPlanState.value.copy(state = InitWordState.COMPLETE)) // 成功

                                }


                            }
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                studyPlanState.value = studyPlanState.value.copy(state = InitWordState.ERROR) // 超时
            } catch (e: Exception) {
                studyPlanState.value =
                    studyPlanState.value.copy(state = InitWordState.ERROR) // 其他异常
            }
        }

    }

    fun onSelectedCount(count: Int) {
        viewModelScope.launch {
            studyPlanState.emit(studyPlanState.value.copy(countInDay = count))
        }
    }

    fun login(userName: String, password: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val loginVo = LoginService.login(userName, password)
                if (loginVo.isSuccess) {
                    MMKVManager.setLoginState(true)
                    Log.i(TAG, "userToken:${loginVo.data.token}")
                    MMKVManager.setUserToken(loginVo.data.token)
                    showToast("登录成功")
                    loadPlans()
                    _isLogin.emit(true)
                } else {
                    errorMsg.emit(loginVo.msg)
                    Log.i(TAG, loginVo.msg)
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
                withContext(Dispatchers.IO) {
                    val users = userDao.getAllUsers()

                    Log.d("MainViewModel", "Users fetched: ${users}")
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Error fetching users", e)
            }
        }
    }
}


