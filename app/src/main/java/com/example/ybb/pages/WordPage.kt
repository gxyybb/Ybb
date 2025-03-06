package com.example.ybb.pages

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ybb.InitWordState
import com.example.ybb.StudyPlanState
import com.example.ybb.api.entity.WordPlan
import com.example.ybb.showToast
import com.example.ybb.utils.MMKVManager
import kotlinx.coroutines.launch
@Composable
fun WordPage(studyPlanState :StudyPlanState ,
             planList: List<WordPlan>,
             onSelectedSubject: (String) -> Unit,
             onSelectedCount: (Int) -> Unit,
             onConfirm: () -> Unit) {
    val navController = rememberNavController()

    // 设置导航图，在 navController 中定义导航路径
    NavHost(navController = navController, startDestination = "first_selected_page") {
        composable("first_selected_page") {
            FirstSelectedPage(navController,
                onSelectedSubject,
                onSelectedCount,
                onConfirm = onConfirm)
        }
        composable("home_page"){
            HomePage(studyPlanState = studyPlanState,
                planList = planList)
        }
    }

    // 判断是否是首次使用，若是则显示首次选择页面
    if (MMKVManager.getWordFirstUsed()) {
        // 直接导航到首次选择页面
        navController.navigate("first_selected_page")
    } else {
        HomePage(studyPlanState,planList)
    }
}



@Composable
fun HomePage(
    studyPlanState: StudyPlanState,
    planList: List<WordPlan>
) {
    when (studyPlanState.state) {
        InitWordState.LOADING -> {
            LoadingScreen()
        }
        InitWordState.ERROR -> {
            ErrorScreen()
        }
        else -> {
            MainScreen(planList)
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("加载中，请稍候...")
        }
    }
}

@Composable
fun ErrorScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("加载失败，请重试", color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { /* 触发重试逻辑 */ }) {
                Text("重试")
            }
        }
    }
}

@Composable
fun MainScreen(planList: List<WordPlan>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("首页", style = MaterialTheme.typography.titleLarge)
    }
}



@Composable
fun FirstSelectedPage(
    navController: NavController,
    onSelectedSubject: (String) -> Unit,
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
                0 -> SubjectSelectionPage(pagerState, onSelected = onSelectedSubject)  // 传递 pagerState
                1 -> WordCountSelectionPage(onSelected = onSelectedCount, onConfirm = { onConfirm()
                    navController.navigate("home_page")
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
fun SubjectSelectionPage(pagerState: PagerState,onSelected: (String) -> Unit) {
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
        val subjects = listOf("数学", "英语", "物理", "化学")
        subjects.forEach { subject ->
            Button(
                onClick = {
                    // 跳转到下一个页面
                    onSelected(subject)
                    // 使用 pagerState 切换到第 1 页（WordCountSelectionPage）
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = subject)
            }
        }


    }
}


@Composable
fun WordCountSelectionPage(onSelected: (Int) -> Unit,onConfirm:() -> Unit) {
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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(text = "$count 单词")
            }
        }

        Button(onClick = {
            onConfirm()
        }
        ) {
            Text("确定")
        }

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

