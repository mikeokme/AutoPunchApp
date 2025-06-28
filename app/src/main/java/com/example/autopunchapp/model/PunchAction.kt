package com.example.autopunchapp.model

import java.io.Serializable

/**
 * 打卡动作数据类
 * @param appPackageName 应用包名
 * @param actions 动作列表
 */
data class PunchAction(
    val appPackageName: String,
    val actions: List<Action>
) : Serializable 