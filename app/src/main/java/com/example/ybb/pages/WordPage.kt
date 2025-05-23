package com.example.ybb.pages

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ybb.LoadState
import com.example.ybb.MainViewModel
import com.example.ybb.StudyPlanState
import com.example.ybb.TAG
import com.example.ybb.api.entity.Subject
import com.example.ybb.utils.MMKVManager

@Composable
fun WordPage(
    vm: MainViewModel,
    studyPlanState: StudyPlanState,
    subjects: List<Subject>,
    onSelectedSubject: (Int) -> Unit,
    onSelectedCount: (Int) -> Unit,
    onConfirm: () -> Unit
) {
    val planList by vm.planList.collectAsState()
    val navController = rememberNavController()
    val startDestination = if (planList.isEmpty()) "first_selected_page" else "home_page"

    Log.i(TAG, "planList.isEmpty()): ")

    NavHost(navController = navController, startDestination = startDestination) {
        composable("first_selected_page") {
            FirstSelectedPage(
                navController,
                subjects = subjects,
                studyPlanState = studyPlanState,
                onSelectedSubject,
                onSelectedCount,
                onConfirm = {
                    onConfirm()
                    navController.navigate("home_page") {
                        popUpTo("first_selected_page") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("home_page") {
            HomePage(
                vm,
                studyPlanState,
                onSelectedSubject,
                onSelectedCount
            ) {
                navController.navigate("study_plan") {
                    launchSingleTop = true
                }
            }
        }
        composable("study_plan") {
            StudyPlan(
                vm,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}


@Composable
fun HomePage(
    vm: MainViewModel,
    studyPlanState: StudyPlanState,
    onSelectedSubject: (Int) -> Unit,
    onSelectedCount: (Int) -> Unit,
    toStudyPlan: () -> Unit,
) {
    val barUiState by vm.barUiState.collectAsState()
    var showSubjectSelectedPopUp by remember { mutableStateOf(true) }
    var subjectId = barUiState.subjectId

    val isLoading by vm.isLoading.collectAsState()
    val planList by vm.planList.collectAsState()

    // 学科选择后重新加载计划列表


    if (subjectId == 0 && showSubjectSelectedPopUp) {
        AlertDialog(
            onDismissRequest = { showSubjectSelectedPopUp = false },
            title = { Text("请选择计划") },
            text = {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    LazyColumn {
                        items(planList) { plan ->
                            Text(
                                text = plan.subject?.subjectName.orEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        plan.subject?.let {
                                            onSelectedSubject(it.id)
                                            MMKVManager.setWordPlan(true)
                                            onSelectedCount(plan.countByDay.toInt())
                                            vm.initBarUiState(plan)
                                        }
                                        showSubjectSelectedPopUp = false
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    } else {


        // ✅ subjectId 有效，展示主界面
        MainScreen(
            vm = vm,
            studyPlanState = studyPlanState,
            toStudyPlan = toStudyPlan,
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: MainViewModel, studyPlanState: StudyPlanState, toStudyPlan: () -> Unit) {
    val barLoadState by vm.barLoadState.collectAsState()
    val barUiState by vm.barUiState.collectAsState()
    val centerInfo by vm.centerInfo.collectAsState()
    val topInfo by vm.topBarInfo.collectAsState()
    val bottomInfo by vm.bottomBarInfo.collectAsState()
    var mode by remember { mutableStateOf(CenterItemMode.SHOW_ALL) }
    var showSheet by remember { mutableStateOf(false) }
    val options = listOf(
        CenterItemMode.SHOW_ALL to "中英显示",
        CenterItemMode.COVER_CHINESE to "遮住中文",
        CenterItemMode.COVER_ENGLISH to "遮住英文"
    )
    var showChat by remember { mutableStateOf(false) }
    var showWordLearning by remember { mutableStateOf(false) }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "当前展示模式：${
                        when (mode) {
                            CenterItemMode.SHOW_ALL -> "中英显示"
                            CenterItemMode.COVER_CHINESE -> "遮住中文"
                            CenterItemMode.COVER_ENGLISH -> "遮住英文"
                        }
                    }", style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFFE0E0E0))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    options.forEach { (value, label) ->
                        val selected = value == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) Color.White else Color.Transparent)
                                .clickable { mode = value }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (selected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        showSheet = false
                        showWordLearning = true
                              },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(50), // 圆角按钮
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // 背景色
                        contentColor = Color.White // 文本颜色
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(text = "单词拼写", style = MaterialTheme.typography.labelLarge)
                }

            }
        }
    }
    when (barLoadState) {
        is LoadState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator() // 或自定义动画
            }
        }

        is LoadState.Success -> {
            Scaffold(
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(top = 30.dp, bottom = 10.dp)
                    ) {
                        // 中间导航按钮组
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                modifier = Modifier
                                    .clickable(enabled = barUiState.day > 0) {
                                        if (barUiState.day > 0) {
                                            vm.dayClick(barUiState.day - 1)
                                        }
                                    }
                                    .then(
                                        Modifier.graphicsLayer(
                                            alpha = if (barUiState.day == 0) 0.5f else 1f
                                        )
                                    ),
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "上"
                            )
                            Row(
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .clickable {
                                        showSheet = !showSheet
                                    }) {
                                Text(text = "Day${barUiState.day}", fontSize = 20.sp)
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "展开或收起",
                                    modifier = Modifier.rotate(if (showSheet) 180f else 0f)
                                )
                            }
                            Icon(
                                modifier = Modifier
                                    .clickable(enabled = barUiState.day < barUiState.totalDays && barUiState.isDayComplete) {
                                        if (barUiState.day < barUiState.totalDays) {
                                            vm.dayClick(barUiState.day + 1)
                                        }
                                    }
                                    .then(
                                        Modifier.graphicsLayer(
                                            alpha = if (barUiState.day == barUiState.totalDays || !barUiState.isDayComplete) 0.5f else 1f
                                        )
                                    ),
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "下"
                            )
                        }

                        // 最右侧 Build 图标
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 16.dp)
                                .clickable {
                                    showChat = true
                                }
                        )
                    }
                }
                ,
                bottomBar = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(bottom = 30.dp, top = 10.dp)
                    ) {
                        LazyRow(modifier = Modifier.weight(5f)) {
                            itemsIndexed(bottomInfo) { index, bottomInfo ->

                                Box(
                                    modifier =
                                        Modifier
                                            .padding(horizontal = 10.dp)
                                            .background(
                                                if (barUiState.bottomIndex == index) Color(
                                                    0xffaaaaaa
                                                ) else Color.White,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .clickable {
                                                vm.onChangeBottom(index)
                                            },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "List ${bottomInfo.wordGroupId}",
                                        textAlign = TextAlign.Center,
                                        color = Color(
                                            if (bottomInfo.completionStatus == 0) {
                                                if (barUiState.bottomIndex == index) 0xff000000 else 0xffaaaaaa
                                            } else {
                                                0xff00aa00
                                            }
                                        )
                                    )
                                }


                            }
                        }
                        Icon(modifier = Modifier
                            .weight(1f)
                            .clickable {
                                Log.d("WordPage", "toStudyPlan clicked")
                                toStudyPlan.invoke()
                            }, imageVector = Icons.Default.DateRange, contentDescription = "计划")
                    }

                }
            ) { paddingValues ->
                LazyColumn(
                    contentPadding = paddingValues,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(centerInfo) { index, word ->
                        CenterItem(index + 1, word, mode)
                    }
                    item {
                        if (barUiState.bottomItem?.completionStatus == 1) {
                            Text(
                                text = "完成 List${barUiState.wordGroupNumber}学习 (๑•̀ㅂ•́)و✧",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        } else {
                            Button(
                                onClick = { barUiState.bottomItem?.let { vm.completeList(it) } },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .wrapContentWidth(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00C2A8), // 青绿色
                                    contentColor = Color.White         // 文字颜色
                                )
                            ) {
                                Text("List打卡")
                            }
                        }
                    }

                }
            }
        }

        is LoadState.Error -> {
            // 显示错误信息
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "加载失败：${(barLoadState as LoadState.Error).message ?: "未知错误"}")
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        else -> {}
    }
    if (showChat){
        ChatUI(vm){
            showChat = false
        }
    }

    if(showWordLearning){
        SpellingTestScreen(centerInfo) {
            showWordLearning = false
        }
    }

}






