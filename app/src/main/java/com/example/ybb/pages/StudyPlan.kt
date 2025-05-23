package com.example.ybb.pages

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ybb.MainViewModel
import com.example.ybb.utils.MMKVManager


/**
 *@author:郭炫烨
 *
 */
@Composable
fun StudyPlan(vm: MainViewModel, onBack: () -> Unit) {
    val barUiState by vm.barUiState.collectAsState()
    val studyForm by vm.studyForm.collectAsState()
    val planList by vm.planList.collectAsState()

    LaunchedEffect(Unit) {
        vm.getStudyForm()
    }

    val finishedColor = Color(0xFF00C2A8)
    val unfinishedColor = Color(0xFFAAAAAA)
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 30.dp, bottom = 10.dp)
    ) {
        if (showDialog)
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("请选择计划") },
                text = {

                    LazyColumn {
                        items(planList) { plan ->
                            Text(
                                text = plan.subject?.subjectName.orEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        plan.subject?.let {
                                            vm.onSelectedSubject(it.id)
                                            showDialog = false
                                        }
                                    }
                                    .padding(8.dp)
                            )
                        }
                    }

                },
                confirmButton = {}
            )
        Column {
            // 顶部行：返回 + 标题
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回",
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 10.dp)
                        .clickable { onBack() }
                )

                Text(
                    text = "学习计划",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Text(
                text = "在学书籍",
                modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = barUiState.plan.subject?.subjectName.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Text("修改  >", modifier = Modifier.padding(end = 8.dp).clickable{
                    showDialog = true
                })
            }

            // 计算进度，避免除以0导致NaN
            val progress = if (studyForm.count() > 0) {
                (0.0f + studyForm.count { it.completionStatus == 1 }) / studyForm.count()
            } else {
                0f  // 如果studyForm为空，则将进度设置为0
            }

            RoundedLinearProgressBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )


            Row(  modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("${studyForm.count { it.completionStatus == 1 }} / ${studyForm.count()}")
                Spacer(modifier = Modifier.weight(1f))
                Text("剩余${barUiState.totalDays - barUiState.completeDays}天")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ 学习计划表展示
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                studyForm.groupBy { it.dateGroupId }

                    .forEach { (dateGroupId, items) ->
                        val allCompleted = items.any { it.completionStatus == 1 }

                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Day$dateGroupId",
                                    color = if (allCompleted) finishedColor else unfinishedColor,
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .width(80.dp)
                                )

                                items.forEach { word ->
                                    Text(
                                        text = "List ${word.wordGroupId}",
                                        color = if (word.completionStatus == 1) finishedColor else unfinishedColor,
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                    )
                                }
                            }
                        }
                    }
            }
        }
    }
}

@Composable
fun RoundedLinearProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.LightGray,
    progressColor: Color = Color(0xFF00C2A8)
) {
    Box(
        modifier = modifier
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
    ) {
        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 500),
            label = "progress"
        )

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .background(progressColor)
        )
    }
}
