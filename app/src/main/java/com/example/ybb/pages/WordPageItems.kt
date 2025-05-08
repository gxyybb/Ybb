package com.example.ybb.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ybb.api.entity.Word


/**
 *@author:郭炫烨
 *
 */
enum class CenterItemMode {
    COVER_ENGLISH, COVER_CHINESE, SHOW_ALL
}

@Composable
fun CenterItem(
    index: Int,
    word: Word,
    mode: CenterItemMode
) {
    // 初始显示状态
    var showEnglish by remember {
        mutableStateOf(mode == CenterItemMode.SHOW_ALL || mode == CenterItemMode.COVER_CHINESE)
    }
    var showChinese by remember {
        mutableStateOf(mode == CenterItemMode.SHOW_ALL || mode == CenterItemMode.COVER_ENGLISH)
    }

    // 使用 LaunchedEffect 来监听 mode 的变化
    LaunchedEffect(mode) {
        showEnglish = mode == CenterItemMode.SHOW_ALL || mode == CenterItemMode.COVER_CHINESE
        showChinese = mode == CenterItemMode.SHOW_ALL || mode == CenterItemMode.COVER_ENGLISH
    }

    Row(
        modifier = Modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xffeeeeee))
            .padding(start = 8.dp)
    ) {
        // 显示索引
        Text(text = "$index", modifier = Modifier.padding(end = 8.dp))

        // 英文部分
        Column(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
        ) {
            if (showEnglish) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            if (mode == CenterItemMode.COVER_ENGLISH) {
                                showEnglish = false
                            }
                        }

                ) {
                    Column() {
                        Text(
                            text = word.word.orEmpty(),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Text(
                            text = "英${word.pronunciation.orEmpty()}",
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

            } else {
                // 遮挡英文，增加点击区域大小
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray)
                        .clickable { showEnglish = true }
                        .padding(16.dp) // 扩展点击区域
                )
            }
        }

        // 中文部分
        Column(
            modifier = Modifier
                .weight(1f)
                .height(60.dp)
                .align(Alignment.CenterVertically) // 保证中文垂直居中
        ) {
            if (showChinese) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp)
                        .clickable {
                            if (mode == CenterItemMode.COVER_CHINESE) {
                                showChinese = false
                            }
                        }
                        .padding(16.dp) // 扩展点击区域
                ) {
                    Text(
                        text = word.meaning.orEmpty(),
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .align(Alignment.Center)
                    )
                }
            } else {
                // 遮挡中文，增加点击区域大小
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray)
                        .padding(start = 16.dp)
                        .clickable { showChinese = true }
                        .padding(16.dp) // 扩展点击区域
                )
            }
        }
    }
}

