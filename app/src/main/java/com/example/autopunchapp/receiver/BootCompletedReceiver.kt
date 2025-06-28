package com.example.autopunchapp.receiver

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.autopunchapp.service.PunchJobService
import com.example.autopunchapp.utils.PreferenceManager

/**
 * 开机启动接收器
 */
class BootCompletedReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "设备启动完成")
            
            val preferenceManager = PreferenceManager(context)
            
            // 检查是否启用自动启动
            if (preferenceManager.getAutoStart()) {
                schedulePunchJob(context)
                Log.d(TAG, "已安排打卡任务")
            } else {
                Log.d(TAG, "自动启动已禁用")
            }
        }
    }
    
    /**
     * 安排打卡任务
     */
    private fun schedulePunchJob(context: Context) {
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        
        val componentName = ComponentName(context, PunchJobService::class.java)
        
        val jobInfo = JobInfo.Builder(PunchJobService.JOB_ID, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setPeriodic(15 * 60 * 1000) // 每15分钟检查一次
            .setPersisted(true) // 重启后保持
            .build()
        
        val resultCode = jobScheduler.schedule(jobInfo)
        
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "打卡任务安排成功")
        } else {
            Log.e(TAG, "打卡任务安排失败")
        }
    }
} 