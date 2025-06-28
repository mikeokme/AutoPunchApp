package com.example.autopunchapp.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.autopunchapp.model.Action
import com.example.autopunchapp.model.ActionType
import com.example.autopunchapp.model.PunchAction
import com.example.autopunchapp.utils.PreferenceManager
import java.util.*

/**
 * 自动打卡无障碍服务
 */
class AutoPunchAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "AutoPunchAccessibilityService"
        private var instance: AutoPunchAccessibilityService? = null
        private var isRecording = false
        private val recordedActions = mutableListOf<Action>()
        private var recordingStartTime = 0L
        
        fun getInstance(): AutoPunchAccessibilityService? = instance
        
        fun startRecording() {
            isRecording = true
            recordingStartTime = System.currentTimeMillis()
            recordedActions.clear()
            Log.d(TAG, "开始录制用户操作")
        }
        
        fun stopRecording(): List<Action> {
            isRecording = false
            val actions = recordedActions.toList()
            recordedActions.clear()
            Log.d(TAG, "停止录制，共录制 ${actions.size} 个操作")
            return actions
        }
        
        fun isCurrentlyRecording(): Boolean = isRecording
    }
    
    private lateinit var preferenceManager: PreferenceManager
    private var currentPunchAction: PunchAction? = null
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        preferenceManager = PreferenceManager(this)
        
        Log.d(TAG, "无障碍服务已连接")
        
        // 配置无障碍服务
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
        
        serviceInfo = info
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isRecording) return
        
        try {
            when (event.eventType) {
                AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                    recordClickAction(event)
                }
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED -> {
                    recordTextInputAction(event)
                }
                AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                    recordScrollAction(event)
                }
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    recordWindowChangeAction(event)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "处理无障碍事件时出错", e)
        }
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "无障碍服务被中断")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "无障碍服务已销毁")
    }
    
    /**
     * 处理窗口状态变化事件
     */
    private fun handleWindowStateChanged(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        Log.d(TAG, "窗口状态变化: $packageName")
        
        // 检查是否是目标应用
        currentPunchAction?.let { punchAction ->
            if (packageName == punchAction.appPackageName) {
                // 延迟执行动作，等待界面加载完成
                android.os.Handler(mainLooper).postDelayed({
                    executePunchActions(punchAction)
                }, 1000)
            }
        }
    }
    
    /**
     * 处理窗口内容变化事件
     */
    private fun handleWindowContentChanged(event: AccessibilityEvent) {
        // 可以在这里处理界面元素变化
    }
    
    /**
     * 设置打卡动作
     */
    fun setPunchAction(punchAction: PunchAction) {
        currentPunchAction = punchAction
        Log.d(TAG, "设置打卡动作: ${punchAction.appPackageName}")
    }
    
    /**
     * 执行打卡动作
     */
    private fun executePunchActions(punchAction: PunchAction) {
        Log.d(TAG, "开始执行打卡动作")
        
        punchAction.actions.forEachIndexed { index, action ->
            android.os.Handler(mainLooper).postDelayed({
                executeAction(action)
            }, (index * 500).toLong()) // 每个动作间隔500ms
        }
    }
    
    /**
     * 执行单个动作
     */
    private fun executeAction(action: Action) {
        when (action.type) {
            ActionType.CLICK -> performClick(action.x, action.y)
            ActionType.LONG_PRESS -> performLongPress(action.x, action.y, action.duration)
            ActionType.SWIPE -> performSwipe(action.x, action.y, action.duration)
            ActionType.WAIT -> {
                // 等待操作，不需要执行任何手势
                Log.d(TAG, "等待 ${action.duration}ms")
            }
        }
    }
    
    /**
     * 执行点击操作
     */
    private fun performClick(x: Float, y: Float) {
        val path = Path()
        path.moveTo(x, y)
        
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, 100))
        
        dispatchGesture(gestureBuilder.build(), null, null)
        Log.d(TAG, "执行点击: ($x, $y)")
    }
    
    /**
     * 执行长按操作
     */
    private fun performLongPress(x: Float, y: Float, duration: Long) {
        val path = Path()
        path.moveTo(x, y)
        
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))
        
        dispatchGesture(gestureBuilder.build(), null, null)
        Log.d(TAG, "执行长按: ($x, $y) 持续 ${duration}ms")
    }
    
    /**
     * 执行滑动操作
     */
    private fun performSwipe(x: Float, y: Float, duration: Long) {
        val path = Path()
        path.moveTo(x, y)
        path.lineTo(x + 100, y + 100) // 简单的滑动路径
        
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))
        
        dispatchGesture(gestureBuilder.build(), null, null)
        Log.d(TAG, "执行滑动: ($x, $y) 持续 ${duration}ms")
    }
    
    /**
     * 查找并点击指定文本的按钮
     */
    fun findAndClickButton(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        if (nodes.isNotEmpty()) {
            val node = nodes[0]
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "找到并点击按钮: $text")
                return true
            }
        }
        
        Log.d(TAG, "未找到可点击的按钮: $text")
        return false
    }
    
    /**
     * 查找并点击指定ID的按钮
     */
    fun findAndClickButtonById(viewId: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        
        val nodes = rootNode.findAccessibilityNodeInfosByViewId(viewId)
        if (nodes.isNotEmpty()) {
            val node = nodes[0]
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Log.d(TAG, "找到并点击按钮ID: $viewId")
                return true
            }
        }
        
        Log.d(TAG, "未找到可点击的按钮ID: $viewId")
        return false
    }
    
    private fun recordClickAction(event: AccessibilityEvent) {
        val nodeInfo = event.source ?: return
        val bounds = Rect()
        nodeInfo.getBoundsInScreen(bounds)
        val centerX = (bounds.left + bounds.right) / 2f
        val centerY = (bounds.top + bounds.bottom) / 2f
        
        val action = Action(
            type = ActionType.CLICK,
            x = centerX,
            y = centerY,
            duration = 100
        )
        recordedActions.add(action)
        Log.d(TAG, "记录点击操作: ${nodeInfo.text}")
    }
    
    private fun recordTextInputAction(event: AccessibilityEvent) {
        val nodeInfo = event.source ?: return
        val bounds = Rect()
        nodeInfo.getBoundsInScreen(bounds)
        val centerX = (bounds.left + bounds.right) / 2f
        val centerY = (bounds.top + bounds.bottom) / 2f
        
        val action = Action(
            type = ActionType.CLICK,
            x = centerX,
            y = centerY,
            duration = 100
        )
        recordedActions.add(action)
        Log.d(TAG, "记录文本输入: ${event.text?.joinToString("")}")
    }
    
    private fun recordScrollAction(event: AccessibilityEvent) {
        val nodeInfo = event.source ?: return
        val bounds = Rect()
        nodeInfo.getBoundsInScreen(bounds)
        val centerX = (bounds.left + bounds.right) / 2f
        val centerY = (bounds.top + bounds.bottom) / 2f
        
        val action = Action(
            type = ActionType.SWIPE,
            x = centerX,
            y = centerY,
            duration = 500
        )
        recordedActions.add(action)
        Log.d(TAG, "记录滚动操作")
    }
    
    private fun recordWindowChangeAction(event: AccessibilityEvent) {
        // 窗口变化不需要具体的坐标，使用默认值
        val action = Action(
            type = ActionType.WAIT,
            x = 0f,
            y = 0f,
            duration = 1000
        )
        recordedActions.add(action)
        Log.d(TAG, "记录窗口变化: ${event.text?.joinToString("")}")
    }
} 