package com.example.autopunchapp.model

/**
 * 动作类型枚举
 */
enum class ActionType {
    CLICK_BY_ID, CLICK_BY_TEXT, CLICK_AT, INPUT_TEXT, SWIPE, DELAY, LAUNCH_APP, PRESS_KEY
}

/**
 * 动作数据类
 */
data class Action(
    val type: ActionType,
    val actionParams: Map<String, String> = emptyMap()
) 