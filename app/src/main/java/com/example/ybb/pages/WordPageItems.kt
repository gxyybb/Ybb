package com.example.ybb.pages

import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ybb.ChatData
import com.example.ybb.MainViewModel
import com.example.ybb.api.entity.Word
import kotlinx.coroutines.delay


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
// ChatUI.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatUI(vm: MainViewModel, onBack: () -> Unit) {
    var inputText by remember { mutableStateOf("") }
    val chatList by vm.chatList.collectAsState()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 聊天") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                verticalArrangement = Arrangement.Bottom // ✅ 将内容从底部开始排列
            ) {
                item {
                    Spacer(modifier = Modifier.weight(1f)) // ✅ 占据顶部空白，让消息从底部开始推
                }

                items(chatList) { chat ->
                    MessageItem(message = chat)
                }
            }


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("输入消息...") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    if (inputText.isNotBlank()) {
                        vm.chat(inputText)
                        inputText = ""
                    }
                }) {
                    Text("发送")
                }
            }
        }
    }

    // ✅ 新消息后自动滚动到底部
    LaunchedEffect(chatList.size) {
        if (chatList.isNotEmpty()) {
            listState.animateScrollToItem(chatList.size - 1)
        }
    }
}
@Composable
fun MessageItem(message: ChatData) {
    val bgColor = if (message.isMe) Color(0xFFDCF8C6) else Color(0xFFECECEC)
    val shape = if (message.isMe)
        RoundedCornerShape(12.dp, 0.dp, 12.dp, 12.dp)
    else
        RoundedCornerShape(0.dp, 12.dp, 12.dp, 12.dp)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMe) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .background(bgColor, shape)
                .widthIn(max = 280.dp)
                .padding(10.dp)
        ) {
            if (message.isMe) {
                Text(text = message.msg)
            } else {
                MarkdownView(markdown = message.msg)
            }
        }
    }
}
@Composable
fun MarkdownView(markdown: String) {
    SimpleMarkdownText(markdown)
}
@Composable
fun SimpleMarkdownText(text: String) {
    val annotatedString = buildAnnotatedString {
        val boldPattern = Regex("\\*\\*(.*?)\\*\\*")
        val codePattern = Regex("`(.*?)`")

        var currentIndex = 0
        val matches = (boldPattern.findAll(text) + codePattern.findAll(text)).sortedBy { it.range.first }

        for (match in matches) {
            if (currentIndex < match.range.first) {
                append(text.substring(currentIndex, match.range.first))
            }

            when (match.value) {
                else -> {
                    when {
                        match.value.startsWith("**") -> withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(match.groupValues[1])
                        }

                        match.value.startsWith("`") -> withStyle(
                            SpanStyle(
                                background = Color.LightGray,
                                fontFamily = FontFamily.Monospace
                            )
                        ) {
                            append(match.groupValues[1])
                        }
                    }
                }
            }

            currentIndex = match.range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }

    Text(text = annotatedString)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpellingTestScreen(
    wordList: List<Word>,
    onBack: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    val totalCount = wordList.size

    // 用户输入的每个单词的字母集合
    val currentWord = wordList.getOrNull(currentIndex)?.word ?: ""
    val currentMeaning = wordList.getOrNull(currentIndex)?.meaning ?: ""

    // 用一个字符串保存当前输入的字母
    var inputLetters by remember { mutableStateOf("") }
    // 错误单词记录（单词索引）
    val errorWordIndices = remember { mutableStateListOf<Int>() }
    // 是否完成测试
    var isTestFinished by remember { mutableStateOf(false) }

    // 判断当前输入是否完全正确
    fun isCurrentInputCorrect(): Boolean {
        return inputLetters == currentWord
    }

    // 跳到下一个单词
    fun moveNext(skipCurrent: Boolean = false) {
        // 记录错误单词（跳过或输入错误）
        if (skipCurrent || !isCurrentInputCorrect()) {
            if (!errorWordIndices.contains(currentIndex)) {
                errorWordIndices.add(currentIndex)
            }
        }
        inputLetters = ""
        if (currentIndex < totalCount - 1) {
            currentIndex++
        } else {
            // 测试结束
            isTestFinished = true
        }
    }

    // 重新测试
    fun restartTest() {
        currentIndex = 0
        inputLetters = ""
        errorWordIndices.clear()
        isTestFinished = false
    }

    Scaffold(
        topBar = {
            if (!isTestFinished) {
                TopAppBar(
                    title = {
                        Text(text = "${currentIndex + 1} / $totalCount")
                    },
                    navigationIcon = {
                        Text(
                            "返回",
                            modifier = Modifier
                                .clickable { onBack() }
                                .padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    actions = {
                        Text(
                            "跳过",
                            modifier = Modifier
                                .clickable { moveNext(skipCurrent = true) }
                                .padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("测试结果") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isTestFinished) {
                // 拼写测试页面
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 显示中文意思
                    Text(
                        text = currentMeaning,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 横线与字母输入显示
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 依次显示每个字母或下划线
                        for (i in currentWord.indices) {
                            val correctChar = currentWord[i].lowercaseChar()
                            val inputChar = inputLetters.getOrNull(i)?.lowercaseChar()
                            val isCorrect = inputChar == correctChar
                            val displayChar = if (inputChar != null) inputLetters[i] else '_'

                            Text(
                                text = if (inputChar == null) "_" else displayChar.toString(),
                                color = when {
                                    inputChar == null -> Color.Black
                                    isCorrect -> Color.Black
                                    else -> Color.Red
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }

                    // 输入框，限制只能输入一个字符，且只能依次输入
                    OutlinedTextField(
                        value = inputLetters,
                        onValueChange = { newInput ->
                            // 处理删除操作
                            if (newInput.length < inputLetters.length) {
                                inputLetters = newInput
                                return@OutlinedTextField
                            }

                            // 仅允许逐个字符增加
                            if (newInput.length == inputLetters.length + 1) {
                                val nextChar = newInput.last()
                                val expectedChar = currentWord.getOrNull(inputLetters.length)?.lowercaseChar()

                                val isPrevCorrect = inputLetters.length == 0 || inputLetters
                                    .take(inputLetters.length)
                                    .zip(currentWord)
                                    .all { (inputChar, correctChar) ->
                                        inputChar.lowercaseChar() == correctChar.lowercaseChar()
                                    }

                                if (isPrevCorrect) {
                                    // 如果当前之前的输入都是正确的，允许输入下一个字母（不论对错）
                                    inputLetters = newInput

                                    // 如果整词输入完毕，则尝试进入下一词
                                    if (inputLetters.length == currentWord.length && inputLetters.equals(currentWord, ignoreCase = true)) {
                                        moveNext()
                                    }
                                }
                                // 如果之前已经输入错误，则不允许继续输入，必须先删掉
                            }
                        }, modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .alpha(0f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                        visualTransformation = VisualTransformation.None,
                        placeholder = { Text("") },
                    )
                }
            } else {
                // 测试结果页面
                val correctCount = totalCount - errorWordIndices.size
                val errorWords = errorWordIndices.map { wordList[it].word ?: "" }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("测试完成！", style = MaterialTheme.typography.headlineMedium)
                    Text("正确单词数量：$correctCount / $totalCount", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (errorWords.isNotEmpty()) {
                        Text("错误单词：", style = MaterialTheme.typography.titleMedium)
                        errorWords.forEach { ew ->
                            Text("- $ew", style = MaterialTheme.typography.bodyLarge, color = Color.Red)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(onClick = onBack) {
                            Text("完成测试")
                        }
                        Button(onClick = { restartTest() }) {
                            Text("重新测试")
                        }
                    }
                }
            }
        }
    }
}

