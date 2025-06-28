package com.example.autopunchapp.model

/**
 * 动作类型枚举
 */
enum class ActionType {
    CLICK,      // 点击
    LONG_PRESS, // 长按
    SWIPE,      // 滑动
    WAIT        // 等待
}

/**
 * 动作数据类
 */
data class Action(
    val type: ActionType,    // 动作类型
    val x: Float,           // X坐标
    val y: Float,           // Y坐标
    val duration: Long = 100 // 持续时间（毫秒）
) 