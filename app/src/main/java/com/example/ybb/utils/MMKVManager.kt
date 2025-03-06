package com.example.ybb.utils

import com.tencent.mmkv.MMKV


/**
 *@author:郭炫烨
 *
 */


/** MMKV 全局存储管理 */
object MMKVManager {
    private val mmkv: MMKV = MMKV.defaultMMKV()

    /** 存储 & 读取 登录状态 */
    private const val KEY_LOGIN_STATE = "key_login_state"
    /**是否首次使用**/
    private const val WORD_FIRST_USED = "word_first_used"

    /**是否有学习计划**/
    private const val WORD_PLAN = "word_plan"

    /** 存储 & 读取 用户 Token */
    private const val KEY_USER_TOKEN = "key_user_token"

    /** 存储 & 读取 用户名 */
    private const val KEY_USERNAME = "key_username"

    fun setWordFirstUsed(isFirst:Boolean){
        mmkv.encode(WORD_FIRST_USED,isFirst)
    }
    fun getWordFirstUsed()=
        mmkv.getBoolean(WORD_FIRST_USED,true)


    fun setWordPlan(hasPlan:Boolean){
        mmkv.encode(WORD_PLAN, hasPlan)
    }
    fun getWordPlan() = mmkv.getBoolean(WORD_PLAN,false)

    fun setLoginState(isLoggedIn: Boolean) {
        mmkv.encode(KEY_LOGIN_STATE, isLoggedIn)
    }


    fun getLoginState(): Boolean {
        return mmkv.getBoolean(KEY_LOGIN_STATE, false)
    }



    fun setUserToken(token: String) {
        mmkv.encode(KEY_USER_TOKEN, token)
    }

    fun getUserToken(): String? {
        return mmkv.decodeString(KEY_USER_TOKEN, null)
    }



    fun setUsername(username: String) {
        mmkv.encode(KEY_USERNAME, username)
    }

    fun getUsername(): String? {
        return mmkv.decodeString(KEY_USERNAME, null)
    }

    /** 清除所有数据 */
    fun clearAll() {
        mmkv.clearAll()
    }
}
