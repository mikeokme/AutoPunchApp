package com.example.autopunchapp.model

import java.io.Serializable

/**
 * 打卡日志数据类
 * @param date 打卡日期
 * @param time 打卡时间
 * @param isSuccessful 打卡是否成功
 */
data class PunchLog(
    val date: String,
    val time: String,
    val isSuccessful: Boolean
) : Serializable