package com.example.autopunchapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.autopunchapp.model.PunchLog
import com.example.autopunchapp.model.PunchTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log

/**
 * 偏好设置管理类，用于保存和读取应用程序设置
 */
class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    companion object {
        fun saveSelectedApp(packageName: String) {
            // TODO: 实现静态保存方法
            Log.d(TAG, "Static saveSelectedApp called with: $packageName")
        }

        private const val PREF_NAME = "autopunch_preferences"
        private const val KEY_PUNCH_STATUS = "punch_status"
        private const val KEY_LAST_PUNCH_TIME = "last_punch_time"
        private const val KEY_SELECTED_APP = "selected_app"
        private const val KEY_ACTION_RECORDED = "action_recorded"
        private const val KEY_PUNCH_LOGS = "punch_logs"
        private const val KEY_PUNCH_TASKS = "punch_tasks"
        private const val KEY_START_TIME = "start_time"
        private const val KEY_END_TIME = "end_time"
        private const val KEY_MIN_INTERVAL = "min_interval"
        private const val KEY_MAX_INTERVAL = "max_interval"
        private const val KEY_RANDOM_ENABLED = "random_enabled"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_SHOW_LOG = "show_log"
        private const val KEY_AUTO_START = "auto_start"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_DAILY_REMINDER = "daily_reminder"
        private const val KEY_PUNCH_COMPLETED_NOTIFICATION = "punch_completed_notification"
        private const val KEY_REMINDER_TIME = "reminder_time"
        private const val TAG = "PreferenceManager"
    }

    // 打卡状态相关
    fun savePunchStatus(isCompleted: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_PUNCH_STATUS, isCompleted).apply()
    }

    fun getPunchStatus(): Boolean {
        return sharedPreferences.getBoolean(KEY_PUNCH_STATUS, false)
    }

    fun saveLastPunchTime(time: String) {
        sharedPreferences.edit().putString(KEY_LAST_PUNCH_TIME, time).apply()
    }

    fun getLastPunchTime(): String {
        return sharedPreferences.getString(KEY_LAST_PUNCH_TIME, "") ?: ""
    }

    // 应用选择相关
    fun saveSelectedApp(context: Context, packageName: String) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_SELECTED_APP, packageName).apply()
            Log.d(TAG, "Saved selected app: $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving selected app", e)
        }
    }

    fun getSelectedApp(context: Context): String {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.getString(KEY_SELECTED_APP, "") ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "Error getting selected app", e)
            ""
        }
    }

    // 动作录制相关
    fun saveActionRecordStatus(isRecorded: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ACTION_RECORDED, isRecorded).apply()
    }

    fun getActionRecordStatus(): Boolean {
        return sharedPreferences.getBoolean(KEY_ACTION_RECORDED, false)
    }

    // 打卡日志相关
    fun savePunchLog(log: PunchLog) {
        val logs = getPunchLogs().toMutableList()
        logs.add(0, log) // 添加到列表开头
        val json = gson.toJson(logs)
        sharedPreferences.edit().putString(KEY_PUNCH_LOGS, json).apply()
    }

    fun getPunchLogs(): List<PunchLog> {
        val json = sharedPreferences.getString(KEY_PUNCH_LOGS, null) ?: return emptyList()
        val type = object : TypeToken<List<PunchLog>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearPunchLogs() {
        sharedPreferences.edit().remove(KEY_PUNCH_LOGS).apply()
    }

    // 打卡任务相关
    fun savePunchTask(task: PunchTask) {
        val tasks = getPunchTasks().toMutableList()
        // 检查是否已存在相同ID的任务，如果存在则更新
        val existingIndex = tasks.indexOfFirst { it.id == task.id }
        if (existingIndex != -1) {
            tasks[existingIndex] = task
        } else {
            tasks.add(task)
        }
        val json = gson.toJson(tasks)
        sharedPreferences.edit().putString(KEY_PUNCH_TASKS, json).apply()
    }

    fun getPunchTasks(): List<PunchTask> {
        val json = sharedPreferences.getString(KEY_PUNCH_TASKS, null) ?: return emptyList()
        val type = object : TypeToken<List<PunchTask>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deletePunchTask(taskId: String) {
        val tasks = getPunchTasks().toMutableList()
        tasks.removeAll { it.id == taskId }
        val json = gson.toJson(tasks)
        sharedPreferences.edit().putString(KEY_PUNCH_TASKS, json).apply()
    }

    // 时间设置相关
    fun saveTimeSettings(startTime: String, endTime: String, minInterval: Int, maxInterval: Int, randomEnabled: Boolean) {
        sharedPreferences.edit().apply {
            putString(KEY_START_TIME, startTime)
            putString(KEY_END_TIME, endTime)
            putInt(KEY_MIN_INTERVAL, minInterval)
            putInt(KEY_MAX_INTERVAL, maxInterval)
            putBoolean(KEY_RANDOM_ENABLED, randomEnabled)
        }.apply()
    }

    fun getStartTime(): String {
        return sharedPreferences.getString(KEY_START_TIME, "09:00") ?: "09:00"
    }

    fun getEndTime(): String {
        return sharedPreferences.getString(KEY_END_TIME, "18:00") ?: "18:00"
    }

    fun getMinInterval(): Int {
        return sharedPreferences.getInt(KEY_MIN_INTERVAL, 1)
    }

    fun getMaxInterval(): Int {
        return sharedPreferences.getInt(KEY_MAX_INTERVAL, 3)
    }

    fun getRandomEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_RANDOM_ENABLED, true)
    }

    // 设置相关
    fun saveNotificationEnabled(context: Context, enabled: Boolean) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
            Log.d(TAG, "Saved notification enabled: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving notification enabled", e)
        }
    }

    fun getNotificationEnabled(context: Context): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.getBoolean(KEY_NOTIFICATION_ENABLED, true)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notification enabled", e)
            true
        }
    }

    fun saveShowLog(show: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SHOW_LOG, show).apply()
    }

    fun getShowLog(): Boolean {
        return sharedPreferences.getBoolean(KEY_SHOW_LOG, true)
    }

    fun saveAutoStart(context: Context, enabled: Boolean) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_AUTO_START, enabled).apply()
            Log.d(TAG, "Saved auto start: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving auto start", e)
        }
    }

    fun getAutoStart(context: Context): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            prefs.getBoolean(KEY_AUTO_START, true)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting auto start", e)
            true
        }
    }

    fun saveLanguage(language: String) {
        sharedPreferences.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun getLanguage(): String {
        return sharedPreferences.getString(KEY_LANGUAGE, "zh") ?: "zh"
    }

    // 通知相关
    fun saveDailyReminder(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DAILY_REMINDER, enabled).apply()
    }

    fun getDailyReminder(): Boolean {
        return sharedPreferences.getBoolean(KEY_DAILY_REMINDER, true)
    }

    fun savePunchCompletedNotification(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_PUNCH_COMPLETED_NOTIFICATION, enabled).apply()
    }

    fun getPunchCompletedNotification(): Boolean {
        return sharedPreferences.getBoolean(KEY_PUNCH_COMPLETED_NOTIFICATION, true)
    }

    fun saveReminderTime(minutes: Int) {
        sharedPreferences.edit().putInt(KEY_REMINDER_TIME, minutes).apply()
    }

    fun getReminderTime(): Int {
        return sharedPreferences.getInt(KEY_REMINDER_TIME, 15)
    }

    // 清除所有设置
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}