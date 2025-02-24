package com.example.ybb

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ybb.api.RetrofitInstance
import com.example.ybb.dao.UserDao
import com.example.ybb.entity.User
import com.example.ybb.ui.theme.YbbTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val vm: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YbbTheme{
                Column {
                    Greeting(
                        modifier = Modifier.padding(top = 80.dp),
                        name = "Android:${vm.add()}"
                    )
                    Button(onClick = {
                        vm.addUser()
                    }) {
                        Text("添加")
                    }

                    Button(onClick = {
                        vm.getAll()
                    }) {
                        Text("日志")
                    }
                    Button(onClick = {
                        vm.asyncApi()
                    }) {
                        Text("网络请求")
                    }
                }

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Greeting("Android")
}

@HiltAndroidApp
class MyApplication : Application() {

    // 使用 companion object 声明一个全局的 context 变量
    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        // 将 applicationContext 赋值给静态变量
        context = applicationContext
    }
}


@HiltViewModel
class MainViewModel @Inject constructor(
) : ViewModel() {

    private val userDao = UserDao::class.java.use()
    // 简单的加法操作
    fun add(): Int {
        return 5
    }


    fun asyncApi(){
        viewModelScope.launch(Dispatchers.IO) {
            val response =   RetrofitInstance.apiService.test()
            Log.i("response",response)
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
