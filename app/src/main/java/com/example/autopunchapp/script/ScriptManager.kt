package com.example.autopunchapp.script

import android.content.Context
import android.util.Log
import com.example.autopunchapp.model.Script
import com.example.autopunchapp.model.ScriptExecutionLog
import com.example.autopunchapp.model.ExecutionStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter

/**
 * 脚本管理器
 * 负责脚本的存储、加载、执行管理
 */
class ScriptManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ScriptManager"
        private const val SCRIPTS_DIR = "scripts"
        private const val SCRIPTS_DB_FILE = "scripts_db.json"
        private const val EXECUTION_LOGS_FILE = "execution_logs.json"
        
        @Volatile
        private var instance: ScriptManager? = null
        
        fun getInstance(context: Context): ScriptManager {
            return instance ?: synchronized(this) {
                instance ?: ScriptManager(context).also { instance = it }
            }
        }
    }
    
    private val gson = Gson()
    private val scriptsDir = File(context.filesDir, SCRIPTS_DIR)
    private val scriptsDbFile = File(context.filesDir, SCRIPTS_DB_FILE)
    private val executionLogsFile = File(context.filesDir, EXECUTION_LOGS_FILE)
    
    private val scripts = mutableListOf<Script>()
    private val executionLogs = mutableListOf<ScriptExecutionLog>()
    private val scriptEngine = ScriptEngine.getInstance(context)
    
    init {
        initDirectories()
        loadScriptsFromStorage()
        loadExecutionLogsFromStorage()
    }
    
    /**
     * 初始化目录
     */
    private fun initDirectories() {
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs()
        }
    }
    
    /**
     * 从存储加载脚本列表
     */
    private fun loadScriptsFromStorage() {
        try {
            if (scriptsDbFile.exists()) {
                val json = scriptsDbFile.readText()
                val type = object : TypeToken<List<Script>>() {}.type
                val loadedScripts = gson.fromJson<List<Script>>(json, type)
                scripts.clear()
                scripts.addAll(loadedScripts)
                Log.d(TAG, "加载了 ${scripts.size} 个脚本")
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载脚本列表失败", e)
        }
    }
    
    /**
     * 保存脚本列表到存储
     */
    private fun saveScriptsToStorage() {
        try {
            val json = gson.toJson(scripts)
            scriptsDbFile.writeText(json)
            Log.d(TAG, "脚本列表已保存")
        } catch (e: Exception) {
            Log.e(TAG, "保存脚本列表失败", e)
        }
    }
    
    /**
     * 从存储加载执行日志
     */
    private fun loadExecutionLogsFromStorage() {
        try {
            if (executionLogsFile.exists()) {
                val json = executionLogsFile.readText()
                val type = object : TypeToken<List<ScriptExecutionLog>>() {}.type
                val loadedLogs = gson.fromJson<List<ScriptExecutionLog>>(json, type)
                executionLogs.clear()
                executionLogs.addAll(loadedLogs)
                Log.d(TAG, "加载了 ${executionLogs.size} 条执行日志")
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载执行日志失败", e)
        }
    }
    
    /**
     * 保存执行日志到存储
     */
    private fun saveExecutionLogsToStorage() {
        try {
            val json = gson.toJson(executionLogs)
            executionLogsFile.writeText(json)
            Log.d(TAG, "执行日志已保存")
        } catch (e: Exception) {
            Log.e(TAG, "保存执行日志失败", e)
        }
    }
    
    /**
     * 获取所有脚本
     */
    fun getAllScripts(): List<Script> = scripts.toList()
    
    /**
     * 根据分类获取脚本
     */
    fun getScriptsByCategory(category: String): List<Script> {
        return scripts.filter { it.category.name == category }
    }
    
    /**
     * 获取启用的脚本
     */
    fun getEnabledScripts(): List<Script> {
        return scripts.filter { it.isEnabled }
    }
    
    /**
     * 获取收藏的脚本
     */
    fun getFavoriteScripts(): List<Script> {
        return scripts.filter { it.isFavorite }
    }
    
    /**
     * 根据ID获取脚本
     */
    fun getScriptById(id: String): Script? {
        return scripts.find { it.id == id }
    }
    
    /**
     * 添加脚本
     */
    fun addScript(script: Script): Boolean {
        return try {
            // 保存脚本文件
            val scriptFile = File(scriptsDir, "${script.id}.js")
            scriptFile.writeText(script.content)
            
            // 更新脚本路径
            val updatedScript = script.copy(filePath = scriptFile.absolutePath)
            
            scripts.add(updatedScript)
            saveScriptsToStorage()
            
            Log.d(TAG, "脚本已添加: ${script.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "添加脚本失败", e)
            false
        }
    }
    
    /**
     * 更新脚本
     */
    fun updateScript(script: Script): Boolean {
        return try {
            val index = scripts.indexOfFirst { it.id == script.id }
            if (index != -1) {
                // 更新脚本文件
                val scriptFile = File(scriptsDir, "${script.id}.js")
                scriptFile.writeText(script.content)
                
                val updatedScript = script.copy(
                    filePath = scriptFile.absolutePath,
                    updateTime = System.currentTimeMillis()
                )
                
                scripts[index] = updatedScript
                saveScriptsToStorage()
                
                Log.d(TAG, "脚本已更新: ${script.name}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "更新脚本失败", e)
            false
        }
    }
    
    /**
     * 删除脚本
     */
    fun deleteScript(scriptId: String): Boolean {
        return try {
            val script = scripts.find { it.id == scriptId }
            if (script != null) {
                // 删除脚本文件
                val scriptFile = File(script.filePath)
                if (scriptFile.exists()) {
                    scriptFile.delete()
                }
                
                scripts.removeAll { it.id == scriptId }
                saveScriptsToStorage()
                
                Log.d(TAG, "脚本已删除: ${script.name}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "删除脚本失败", e)
            false
        }
    }
    
    /**
     * 切换脚本启用状态
     */
    fun toggleScriptEnabled(scriptId: String): Boolean {
        val index = scripts.indexOfFirst { it.id == scriptId }
        if (index != -1) {
            val script = scripts[index]
            scripts[index] = script.copy(isEnabled = !script.isEnabled)
            saveScriptsToStorage()
            return true
        }
        return false
    }
    
    /**
     * 切换脚本收藏状态
     */
    fun toggleScriptFavorite(scriptId: String): Boolean {
        val index = scripts.indexOfFirst { it.id == scriptId }
        if (index != -1) {
            val script = scripts[index]
            scripts[index] = script.copy(isFavorite = !script.isFavorite)
            saveScriptsToStorage()
            return true
        }
        return false
    }
    
    /**
     * 执行脚本
     */
    suspend fun executeScript(scriptId: String): ScriptResult = withContext(Dispatchers.IO) {
        val script = getScriptById(scriptId)
        if (script == null) {
            return@withContext ScriptResult(false, "脚本不存在")
        }
        
        if (!script.isEnabled) {
            return@withContext ScriptResult(false, "脚本已禁用")
        }
        
        // 创建执行日志
        val executionLog = ScriptExecutionLog(
            scriptId = scriptId,
            scriptName = script.name,
            startTime = System.currentTimeMillis()
        )
        executionLogs.add(executionLog)
        saveExecutionLogsToStorage()
        
        try {
            // 执行脚本
            val result = scriptEngine.executeScript(script.content)
            
            // 更新执行日志
            val updatedLog = executionLog.copy(
                endTime = System.currentTimeMillis(),
                status = if (result.success) ExecutionStatus.SUCCESS else ExecutionStatus.FAILED,
                message = result.message,
                output = result.output,
                error = if (!result.success) result.message else ""
            )
            
            val logIndex = executionLogs.indexOfFirst { it.id == executionLog.id }
            if (logIndex != -1) {
                executionLogs[logIndex] = updatedLog
            }
            
            // 更新脚本运行统计
            val scriptIndex = scripts.indexOfFirst { it.id == scriptId }
            if (scriptIndex != -1) {
                val updatedScript = scripts[scriptIndex].copy(
                    lastRunTime = System.currentTimeMillis(),
                    runCount = scripts[scriptIndex].runCount + 1
                )
                scripts[scriptIndex] = updatedScript
            }
            
            saveExecutionLogsToStorage()
            saveScriptsToStorage()
            
            result
        } catch (e: Exception) {
            // 更新执行日志为失败状态
            val updatedLog = executionLog.copy(
                endTime = System.currentTimeMillis(),
                status = ExecutionStatus.FAILED,
                error = e.message ?: "执行异常"
            )
            
            val logIndex = executionLogs.indexOfFirst { it.id == executionLog.id }
            if (logIndex != -1) {
                executionLogs[logIndex] = updatedLog
            }
            saveExecutionLogsToStorage()
            
            ScriptResult(false, "执行失败: ${e.message}")
        }
    }
    
    /**
     * 停止脚本执行
     */
    fun stopScript() {
        scriptEngine.stopScript()
    }
    
    /**
     * 检查脚本是否正在运行
     */
    fun isScriptRunning(): Boolean = scriptEngine.isScriptRunning()
    
    /**
     * 获取执行日志
     */
    fun getExecutionLogs(): List<ScriptExecutionLog> = executionLogs.toList()
    
    /**
     * 获取脚本的执行日志
     */
    fun getScriptExecutionLogs(scriptId: String): List<ScriptExecutionLog> {
        return executionLogs.filter { it.scriptId == scriptId }
    }
    
    /**
     * 清理执行日志
     */
    fun clearExecutionLogs() {
        executionLogs.clear()
        saveExecutionLogsToStorage()
    }
    
    /**
     * 释放资源
     */
    fun release() {
        scriptEngine.release()
    }
} 