package com.example.ybb.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.ybb.StudyPlanState
import com.example.ybb.api.entity.Subject
import com.example.ybb.showToast
import kotlinx.coroutines.launch
import kotlin.collections.forEach


/**
 *@author:郭炫烨
 *
 */

@Composable
fun FirstSelectedPage(
    navController: NavController,

    subjects: List<Subject>,
    studyPlanState: StudyPlanState,
    onSelectedSubject: (Int) -> Unit,
    onSelectedCount: (Int) -> Unit,
    onConfirm: () -> Unit
) {
    // 创建一个 PagerState 来管理页面的滑动状态
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 } // Total number of pages
    )



    Column {
        // Pager 用于实现页面滑动
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            when (page) {
                0 -> SubjectSelectionPage(subjects = subjects,studyPlanState = studyPlanState,pagerState, onSelected = onSelectedSubject)  // 传递 pagerState
                1 -> WordCountSelectionPage(onSelected = onSelectedCount,
                    studyPlanState = studyPlanState,
                    navController = navController,
                    onConfirm = {
                        onConfirm()
                    })  // 另一页
            }
        }

        // 页面指示器，显示当前页面的圆点
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun SubjectSelectionPage(subjects: List<Subject>,studyPlanState: StudyPlanState, pagerState: PagerState, onSelected: (Int) -> Unit) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "选择学习科目", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // 假设科目是一个列表
        subjects.forEach { subject ->
            Button(
                onClick = {
                    // 跳转到下一个页面
                    onSelected(subject.id)
                    // 使用 pagerState 切换到第 1 页（WordCountSelectionPage）
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    if (studyPlanState.subject == subject.id) Color(0xFF6200EE) // 选中态颜色
                    else Color(0xFFE0E0E0) // 非选中态颜色
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = subject.subjectName)
            }
        }


    }
}


@Composable
fun WordCountSelectionPage(onSelected: (Int) -> Unit,onConfirm:() -> Unit,
                           navController: NavController,studyPlanState: StudyPlanState) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "选择每日单词数量", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // 假设单词数量是选择范围
        val wordCounts = listOf(10, 20, 30, 40, 50)
        wordCounts.forEach { count ->
            Button(
                onClick = {
                    onSelected(count)
                    // 完成选择，可以进行后续操作
                    scope.launch {
                        showToast("已选择 $count 个单词", Toast.LENGTH_SHORT)
                    }
                    if (studyPlanState.subject != null && studyPlanState.countInDay!=null){
                        onConfirm()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    if (studyPlanState.countInDay == count) Color(0xFF6200EE) // 选中态颜色
                    else Color(0xFFE0E0E0) // 非选中态颜色
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)

            ) {
                Text(text = "$count 单词")
            }
        }

//        Button(onClick = {
//            onConfirm()
//        }
//        ) {
//            Text("确定")
//        }

    }
}

@Composable
fun HorizontalPagerIndicator(
    pagerState: PagerState,
    modifier: Modifier = Modifier
) {
    // 处理页面指示器的实现（例如展示圆点）
    Row(modifier = modifier) {
        for (index in 0 until pagerState.pageCount) {
            val color = if (index == pagerState.currentPage) Color.Black else Color.Gray
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, shape = CircleShape)
                    .padding(4.dp)
            )
        }
    }
}

