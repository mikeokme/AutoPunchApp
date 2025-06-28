package com.example.autopunchapp.script

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.example.autopunchapp.service.AutoPunchAccessibilityService
import com.example.autopunchapp.utils.AccessibilityUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mozilla.javascript.Context as RhinoContext
import org.mozilla.javascript.Function
import org.mozilla.javascript.Scriptable
import org.mozilla.javascript.ScriptableObject
import java.io.File

/**
 * JavaScript 脚本引擎
 * 提供类似 Hamibot 的脚本执行能力
 */
class ScriptEngine(private val context: Context) {
    
    companion object {
        private const val TAG = "ScriptEngine"
        private var instance: ScriptEngine? = null
        
        fun getInstance(context: Context): ScriptEngine {
            if (instance == null) {
                instance = ScriptEngine(context)
            }
            return instance!!
        }
    }
    
    private var rhinoContext: RhinoContext? = null
    private var scope: Scriptable? = null
    private var isRunning = false
    
    init {
        initRhinoEngine()
    }
    
    private fun initRhinoEngine() {
        try {
            rhinoContext = RhinoContext.enter()
            rhinoContext?.optimizationLevel = -1 // 禁用优化以提高兼容性
            scope = rhinoContext?.initStandardObjects()
            
            setupGlobalAPIs()
            Log.d(TAG, "Rhino 引擎初始化成功")
        } catch (e: Exception) {
            Log.e(TAG, "Rhino 引擎初始化失败", e)
        }
    }
    
    /**
     * 设置全局 API
     */
    private fun setupGlobalAPIs() {
        scope?.let { scriptableScope ->
            // 点击相关 API
            ScriptableObject.putProperty(scriptableScope, "click", object : Function() {
                override fun call(cx: RhinoContext?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any>?): Any? {
                    if (args != null && args.isNotEmpty()) {
                        val target = args[0].toString()
                        if (target.matches(Regex("\\d+,\\d+"))) {
                            // 坐标点击
                            val coords = target.split(",")
                            val x = coords[0].toFloat()
                            val y = coords[1].toFloat()
                            performClick(x, y)
                        } else {
                            // 文本点击
                            findAndClickButton(target)
                        }
                    }
                    return null
                }
                
                override fun getClassName(): String = "Function"
            })
            
            // 文本输入 API
            ScriptableObject.putProperty(scriptableScope, "input", object : Function() {
                override fun call(cx: RhinoContext?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any>?): Any? {
                    if (args != null && args.isNotEmpty()) {
                        val text = args[0].toString()
                        inputText(text)
                    }
                    return null
                }
                
                override fun getClassName(): String = "Function"
            })
            
            // 滑动 API
            ScriptableObject.putProperty(scriptableScope, "swipe", object : Function() {
                override fun call(cx: RhinoContext?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any>?): Any? {
                    if (args != null && args.size >= 4) {
                        val x1 = args[0].toString().toFloat()
                        val y1 = args[1].toString().toFloat()
                        val x2 = args[2].toString().toFloat()
                        val y2 = args[3].toString().toFloat()
                        val duration = if (args.size > 4) args[4].toString().toLong() else 500L
                        performSwipe(x1, y1, x2, y2, duration)
                    }
                    return null
                }
                
                override fun getClassName(): String = "Function"
            })
            
            // 等待 API
            ScriptableObject.putProperty(scriptableScope, "sleep", object : Function() {
                override fun call(cx: RhinoContext?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any>?): Any? {
                    if (args != null && args.isNotEmpty()) {
                        val ms = args[0].toString().toLong()
                        Thread.sleep(ms)
                    }
                    return null
                }
                
                override fun getClassName(): String = "Function"
            })
            
            // 查找元素 API
            ScriptableObject.putProperty(scriptableScope, "findText", object : Function() {
                override fun call(cx: RhinoContext?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any>?): Any? {
                    if (args != null && args.isNotEmpty()) {
                        val text = args[0].toString()
                        val node = findNodeByText(text)
                        if (node != null) {
                            val result = cx?.newObject(scriptableScope)
                            ScriptableObject.putProperty(result, "text", node.text?.toString() ?: "")
                            ScriptableObject.putProperty(result, "bounds", "${node.boundsInScreen.left},${node.boundsInScreen.top},${node.boundsInScreen.right},${node.boundsInScreen.bottom}")
                            ScriptableObject.putProperty(result, "clickable", node.isClickable)
                            return result
                        }
                    }
                    return null
                }
                
                override fun getClassName(): String = "Function"
            })
            
            // 启动应用 API
            ScriptableObject.putProperty(scriptableScope, "launchApp", object : Function() {
                override fun call(cx: RhinoContext?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any>?): Any? {
                    if (args != null && args.isNotEmpty()) {
                        val packageName = args[0].toString()
                        launchApp(packageName)
                    }
                    return null
                }
                
                override fun getClassName(): String = "Function"
            })
            
            // 日志 API
            ScriptableObject.putProperty(scriptableScope, "log", object : Function() {
                override fun call(cx: RhinoContext?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any>?): Any? {
                    if (args != null && args.isNotEmpty()) {
                        Log.d(TAG, "Script: ${args[0]}")
                    }
                    return null
                }
                
                override fun getClassName(): String = "Function"
            })
            
            // 获取屏幕信息 API
            ScriptableObject.putProperty(scriptableScope, "getScreenSize", object : Function() {
                override fun call(cx: RhinoContext?, scope: Scriptable?, thisObj: Scriptable?, args: Array<out Any>?): Any? {
                    val screenSize = getScreenSize()
                    val result = cx?.newObject(scriptableScope)
                    ScriptableObject.putProperty(result, "width", screenSize.first)
                    ScriptableObject.putProperty(result, "height", screenSize.second)
                    return result
                }
                
                override fun getClassName(): String = "Function"
            })
        }
    }
    
    /**
     * 执行脚本
     */
    suspend fun executeScript(scriptContent: String): ScriptResult = withContext(Dispatchers.IO) {
        val result = ScriptResult()
        
        try {
            isRunning = true
            Log.d(TAG, "开始执行脚本")
            
            rhinoContext?.evaluateString(scope, scriptContent, "Script", 1, null)
            
            result.success = true
            result.message = "脚本执行成功"
            Log.d(TAG, "脚本执行完成")
            
        } catch (e: Exception) {
            result.success = false
            result.message = "脚本执行失败: ${e.message}"
            Log.e(TAG, "脚本执行失败", e)
        } finally {
            isRunning = false
        }
        
        result
    }
    
    /**
     * 执行脚本文件
     */
    suspend fun executeScriptFile(filePath: String): ScriptResult {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                val scriptContent = file.readText()
                executeScript(scriptContent)
            } else {
                ScriptResult(false, "脚本文件不存在: $filePath")
            }
        } catch (e: Exception) {
            ScriptResult(false, "读取脚本文件失败: ${e.message}")
        }
    }
    
    /**
     * 停止脚本执行
     */
    fun stopScript() {
        isRunning = false
        Log.d(TAG, "脚本执行已停止")
    }
    
    /**
     * 检查是否正在运行
     */
    fun isScriptRunning(): Boolean = isRunning
    
    /**
     * 释放资源
     */
    fun release() {
        try {
            isRunning = false
            RhinoContext.exit()
            rhinoContext = null
            scope = null
            Log.d(TAG, "脚本引擎资源已释放")
        } catch (e: Exception) {
            Log.e(TAG, "释放脚本引擎资源失败", e)
        }
    }
    
    // 自动化操作的具体实现
    private fun performClick(x: Float, y: Float) {
        AutoPunchAccessibilityService.getInstance()?.performClick(x, y)
    }
    
    private fun findAndClickButton(text: String) {
        AutoPunchAccessibilityService.getInstance()?.findAndClickButton(text)
    }
    
    private fun inputText(text: String) {
        AutoPunchAccessibilityService.getInstance()?.inputText(text)
    }
    
    private fun performSwipe(x1: Float, y1: Float, x2: Float, y2: Float, duration: Long) {
        AutoPunchAccessibilityService.getInstance()?.performSwipeCustom(x1, y1, x2, y2, duration)
    }
    
    private fun findNodeByText(text: String): AccessibilityNodeInfo? {
        return AutoPunchAccessibilityService.getInstance()?.findNodeByText(text)
    }
    
    private fun launchApp(packageName: String) {
        AutoPunchAccessibilityService.getInstance()?.launchApp(packageName)
    }
    
    private fun getScreenSize(): Pair<Int, Int> {
        val displayMetrics = context.resources.displayMetrics
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }
}

/**
 * 脚本执行结果
 */
data class ScriptResult(
    val success: Boolean = false,
    val message: String = "",
    val output: String = "",
    val executionTime: Long = 0
) 