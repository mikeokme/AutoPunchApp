package com.example.autopunchapp.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.autopunchapp.model.PunchAction
import com.example.autopunchapp.model.PunchTask
import com.example.autopunchapp.utils.PreferenceManager
import java.util.*

/**
 * 打卡任务后台服务
 */
class PunchJobService : JobService() {
    
    companion object {
        private const val TAG = "PunchJobService"
        const val JOB_ID = 1001
    }
    
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)
        Log.d(TAG, "打卡任务服务已创建")
    }
    
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "开始执行打卡任务")
        
        // 检查是否有需要执行的打卡任务
        val tasks = preferenceManager.getPunchTasks()
        val currentTime = Calendar.getInstance()
        
        for (task in tasks) {
            if (task.isEnabled && shouldExecuteTask(task, currentTime)) {
                executePunchTask(task)
            }
        }
        
        // 任务执行完成
        jobFinished(params, false)
        return true
    }
    
    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "打卡任务被停止")
        return false
    }
    
    /**
     * 检查是否应该执行任务
     */
    private fun shouldExecuteTask(task: PunchTask, currentTime: Calendar): Boolean {
        val startTime = parseTimeString(task.startTime)
        val endTime = parseTimeString(task.endTime)
        
        val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
        val currentMinute = currentTime.get(Calendar.MINUTE)
        val currentTimeInMinutes = currentHour * 60 + currentMinute
        
        val startTimeInMinutes = startTime.first * 60 + startTime.second
        val endTimeInMinutes = endTime.first * 60 + endTime.second
        
        // 检查是否在时间范围内
        if (currentTimeInMinutes < startTimeInMinutes || currentTimeInMinutes > endTimeInMinutes) {
            return false
        }
        
        // 检查间隔时间
        val lastPunchTime = preferenceManager.getLastPunchTime()
        if (lastPunchTime.isNotEmpty()) {
            val lastTime = parseTimeString(lastPunchTime)
            val lastTimeInMinutes = lastTime.first * 60 + lastTime.second
            val timeDiff = currentTimeInMinutes - lastTimeInMinutes
            
            val minIntervalMinutes = task.minInterval * 60
            val maxIntervalMinutes = task.maxInterval * 60
            
            if (timeDiff < minIntervalMinutes) {
                return false
            }
            
            if (task.isRandomEnabled) {
                // 随机间隔
                val randomInterval = (minIntervalMinutes..maxIntervalMinutes).random()
                if (timeDiff < randomInterval) {
                    return false
                }
            } else {
                // 固定间隔
                if (timeDiff < maxIntervalMinutes) {
                    return false
                }
            }
        }
        
        return true
    }
    
    /**
     * 执行打卡任务
     */
    private fun executePunchTask(task: PunchTask) {
        Log.d(TAG, "执行打卡任务: ${task.appName}")
        
        // 启动目标应用
        val intent = packageManager.getLaunchIntentForPackage(task.appPackageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            
            // 延迟执行打卡动作，等待应用启动
            android.os.Handler(mainLooper).postDelayed({
                performPunchAction(task)
            }, 3000)
        } else {
            Log.e(TAG, "无法启动应用: ${task.appPackageName}")
        }
    }
    
    /**
     * 执行打卡动作
     */
    private fun performPunchAction(task: PunchTask) {
        val accessibilityService = AutoPunchAccessibilityService.getInstance()
        if (accessibilityService != null) {
            // 这里应该从存储中获取录制的动作
            // 暂时使用模拟动作
            val mockAction = PunchAction(
                appPackageName = task.appPackageName,
                actions = listOf(
                    com.example.autopunchapp.model.Action(
                        type = com.example.autopunchapp.model.ActionType.CLICK,
                        x = 500f,
                        y = 500f
                    )
                )
            )
            
            accessibilityService.setPunchAction(mockAction)
            Log.d(TAG, "设置打卡动作完成")
        } else {
            Log.e(TAG, "无障碍服务未启动")
        }
    }
    
    /**
     * 解析时间字符串 (HH:mm 格式)
     */
    private fun parseTimeString(timeString: String): Pair<Int, Int> {
        return try {
            val parts = timeString.split(":")
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            Pair(hour, minute)
        } catch (e: Exception) {
            Log.e(TAG, "解析时间字符串失败: $timeString", e)
            Pair(0, 0)
        }
    }
} 