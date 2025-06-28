package com.example.autopunchapp.model

import java.io.Serializable
import java.util.*

/**
 * 脚本信息数据类
 */
data class Script(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String = "",
    val author: String = "",
    val version: String = "1.0.0",
    val category: ScriptCategory = ScriptCategory.OTHER,
    val tags: List<String> = emptyList(),
    val content: String = "",
    val filePath: String = "",
    val isEnabled: Boolean = true,
    val isFavorite: Boolean = false,
    val downloadCount: Int = 0,
    val rating: Float = 0f,
    val createTime: Long = System.currentTimeMillis(),
    val updateTime: Long = System.currentTimeMillis(),
    val lastRunTime: Long = 0L,
    val runCount: Int = 0,
    val thumbnailUrl: String = "",
    val permissions: List<String> = emptyList(),
    val minApiLevel: Int = 21,
    val maxApiLevel: Int = 34
) : Serializable

/**
 * 脚本分类枚举
 */
enum class ScriptCategory(val displayName: String, val icon: String) {
    PUNCH_IN("打卡签到", "ic_punch"),
    GAME("游戏辅助", "ic_game"),
    SOCIAL("社交应用", "ic_social"),
    SHOPPING("购物优惠", "ic_shopping"),
    TOOL("实用工具", "ic_tool"),
    EDUCATION("学习教育", "ic_education"),
    FINANCE("金融理财", "ic_finance"),
    OTHER("其他", "ic_other")
}

/**
 * 脚本执行记录
 */
data class ScriptExecutionLog(
    val id: String = UUID.randomUUID().toString(),
    val scriptId: String,
    val scriptName: String,
    val startTime: Long,
    val endTime: Long = 0L,
    val status: ExecutionStatus = ExecutionStatus.RUNNING,
    val message: String = "",
    val output: String = "",
    val error: String = ""
) : Serializable

/**
 * 执行状态枚举
 */
enum class ExecutionStatus {
    RUNNING, SUCCESS, FAILED, CANCELLED
}

/**
 * 脚本市场信息
 */
data class ScriptMarketInfo(
    val id: String,
    val name: String,
    val description: String,
    val author: String,
    val version: String,
    val category: ScriptCategory,
    val tags: List<String>,
    val downloadCount: Int,
    val rating: Float,
    val thumbnailUrl: String,
    val downloadUrl: String,
    val fileSize: Long,
    val updateTime: Long,
    val permissions: List<String>,
    val minApiLevel: Int,
    val maxApiLevel: Int
) : Serializable 