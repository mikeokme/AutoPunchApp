package com.example.autopunchapp.model

import java.io.Serializable

/**
 * 打卡任务数据类
 * @param id 任务ID
 * @param appPackageName 应用包名
 * @param appName 应用名称
 * @param startTime 开始时间 (HH:mm 格式)
 * @param endTime 结束时间 (HH:mm 格式)
 * @param minInterval 最小间隔(小时)
 * @param maxInterval 最大间隔(小时)
 * @param isEnabled 是否启用
 * @param isRandomEnabled 是否启用随机打卡
 */
data class PunchTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val appPackageName: String,
    val appName: String,
    val startTime: String,
    val endTime: String,
    val minInterval: Int,
    val maxInterval: Int,
    var isEnabled: Boolean = true,
    var isRandomEnabled: Boolean = true
) : Serializable