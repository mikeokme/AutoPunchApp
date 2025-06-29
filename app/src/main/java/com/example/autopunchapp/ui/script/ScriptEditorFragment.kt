package com.example.autopunchapp.ui.script

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.autopunchapp.R
import com.example.autopunchapp.databinding.FragmentScriptEditorBinding
import com.example.autopunchapp.model.Script
import com.example.autopunchapp.model.ScriptCategory
import com.example.autopunchapp.script.ScriptManager
import com.example.autopunchapp.script.ScriptResult
import kotlinx.coroutines.launch
import java.util.*

class ScriptEditorFragment : Fragment() {
    
    companion object {
        private const val TAG = "ScriptEditorFragment"
        private const val ARG_SCRIPT_ID = "script_id"
        
        fun newInstance(scriptId: String? = null): ScriptEditorFragment {
            return ScriptEditorFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SCRIPT_ID, scriptId)
                }
            }
        }
    }
    
    private var _binding: FragmentScriptEditorBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var scriptManager: ScriptManager
    private var currentScript: Script? = null
    private var isEditing = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScriptEditorBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        scriptManager = ScriptManager.getInstance(requireContext())
        
        setupViews()
        setupListeners()
        loadScript()
        setupCodeEditor()
    }
    
    private fun setupViews() {
        // 设置默认代码模板
        if (currentScript == null) {
            binding.etScriptCode.setText(getDefaultScriptTemplate())
        }
    }
    
    private fun setupListeners() {
        // 返回按钮
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        
        // 保存按钮
        binding.btnSave.setOnClickListener {
            saveScript()
        }
        
        // 运行按钮
        binding.btnRun.setOnClickListener {
            runScript()
        }
        
        // 模板按钮
        binding.btnTemplate.setOnClickListener {
            showTemplateDialog()
        }
        
        // API帮助按钮
        binding.btnApiHelp.setOnClickListener {
            showApiHelpDialog()
        }
        
        // 格式化按钮
        binding.btnFormat.setOnClickListener {
            formatCode()
        }
        
        // 清空输出按钮
        binding.btnClearOutput.setOnClickListener {
            binding.tvOutput.text = ""
        }
        
        // 代码编辑器文本变化监听
        binding.etScriptCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateLineInfo()
            }
        })
    }
    
    private fun loadScript() {
        val scriptId = arguments?.getString(ARG_SCRIPT_ID)
        if (scriptId != null) {
            currentScript = scriptManager.getScriptById(scriptId)
            currentScript?.let { script ->
                isEditing = true
                binding.etScriptName.setText(script.name)
                binding.etScriptDescription.setText(script.description)
                binding.etScriptAuthor.setText(script.author)
                binding.etScriptVersion.setText(script.version)
                binding.etScriptCode.setText(script.content)
            }
        }
    }
    
    private fun setupCodeEditor() {
        // 设置代码编辑器属性
        binding.etScriptCode.apply {
            // 可以在这里添加语法高亮等高级功能
        }
    }
    
    private fun saveScript() {
        val name = binding.etScriptName.text.toString().trim()
        val description = binding.etScriptDescription.text.toString().trim()
        val author = binding.etScriptAuthor.text.toString().trim()
        val version = binding.etScriptVersion.text.toString().trim()
        val content = binding.etScriptCode.text.toString()
        
        if (name.isEmpty()) {
            Toast.makeText(context, "请输入脚本名称", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (content.isEmpty()) {
            Toast.makeText(context, "请输入脚本内容", Toast.LENGTH_SHORT).show()
            return
        }
        
        val script = if (isEditing && currentScript != null) {
            currentScript!!.copy(
                name = name,
                description = description,
                author = author,
                version = version,
                content = content,
                updateTime = System.currentTimeMillis()
            )
        } else {
            Script(
                name = name,
                description = description,
                author = author,
                version = version,
                content = content,
                category = ScriptCategory.OTHER
            )
        }
        
        lifecycleScope.launch {
            val success = if (isEditing) {
                scriptManager.updateScript(script)
            } else {
                scriptManager.addScript(script)
            }
            
            if (success) {
                Toast.makeText(context, "脚本保存成功", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(context, "脚本保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun runScript() {
        val content = binding.etScriptCode.text.toString()
        if (content.isEmpty()) {
            Toast.makeText(context, "请输入脚本内容", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 显示输出区域
        binding.layoutOutput.visibility = View.VISIBLE
        binding.tvOutput.text = "正在执行脚本...\n"
        
        lifecycleScope.launch {
            try {
                val result = scriptManager.executeScript(content)
                
                val output = if (result.success) {
                    "脚本执行成功！\n${result.message}\n${result.output}"
                } else {
                    "脚本执行失败！\n${result.message}"
                }
                
                binding.tvOutput.append(output)
                
            } catch (e: Exception) {
                binding.tvOutput.append("执行异常: ${e.message}\n")
                Log.e(TAG, "脚本执行失败", e)
            }
        }
    }
    
    private fun showTemplateDialog() {
        val templates = arrayOf(
            "基础点击模板",
            "文本输入模板", 
            "滑动操作模板",
            "应用启动模板",
            "循环操作模板",
            "条件判断模板",
            "文件操作模板",
            "网络请求模板",
            "OCR识别模板",
            "定时任务模板"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("选择模板")
            .setItems(templates) { _, which ->
                val template = when (which) {
                    0 -> getClickTemplate()
                    1 -> getInputTemplate()
                    2 -> getSwipeTemplate()
                    3 -> getLaunchAppTemplate()
                    4 -> getLoopTemplate()
                    5 -> getConditionTemplate()
                    6 -> getFileOperationTemplate()
                    7 -> getNetworkTemplate()
                    8 -> getOcrTemplate()
                    9 -> getScheduledTaskTemplate()
                    else -> getDefaultScriptTemplate()
                }
                binding.etScriptCode.setText(template)
            }
            .show()
    }
    
    private fun showApiHelpDialog() {
        val helpText = """
            API 帮助文档：
            
            ===== 基础操作 =====
            点击操作：
            - click("按钮文本") - 点击包含指定文本的按钮
            - click("100,200") - 点击指定坐标
            - longClick("文本", 2000) - 长按文本2秒
            - doubleClick("文本") - 双击文本
            
            文本输入：
            - input("要输入的文本") - 在当前焦点输入文本
            - clearText() - 清空当前输入框
            - setText("新文本") - 设置文本内容
            
            滑动操作：
            - swipe(100, 200, 300, 400, 500) - 从(100,200)滑动到(300,400)，持续500ms
            - swipeUp() - 向上滑动
            - swipeDown() - 向下滑动
            - swipeLeft() - 向左滑动
            - swipeRight() - 向右滑动
            
            ===== 等待操作 =====
            - sleep(1000) - 等待1000毫秒
            - waitForText("文本", 5000) - 等待文本出现，最多5秒
            - waitForElement("元素ID", 3000) - 等待元素出现
            
            ===== 查找元素 =====
            - findText("文本") - 查找包含指定文本的元素
            - findElement("元素ID") - 查找指定ID的元素
            - findElementByClass("类名") - 根据类名查找元素
            - findElementByDesc("描述") - 根据描述查找元素
            - exists("文本") - 检查文本是否存在
            
            ===== 应用操作 =====
            - launchApp("com.example.app") - 启动指定应用
            - closeApp("com.example.app") - 关闭指定应用
            - getCurrentApp() - 获取当前应用包名
            - goHome() - 返回桌面
            - goBack() - 返回上一页
            
            ===== 屏幕操作 =====
            - getScreenSize() - 获取屏幕尺寸
            - takeScreenshot() - 截取屏幕
            - getScreenBrightness() - 获取屏幕亮度
            - setScreenBrightness(100) - 设置屏幕亮度
            
            ===== 系统操作 =====
            - getBatteryLevel() - 获取电池电量
            - isCharging() - 检查是否在充电
            - getNetworkType() - 获取网络类型
            - isWifiConnected() - 检查WiFi连接状态
            
            ===== 文件操作 =====
            - readFile("文件路径") - 读取文件内容
            - writeFile("文件路径", "内容") - 写入文件
            - appendFile("文件路径", "内容") - 追加文件内容
            - deleteFile("文件路径") - 删除文件
            - fileExists("文件路径") - 检查文件是否存在
            
            ===== 网络操作 =====
            - httpGet("URL") - 发送GET请求
            - httpPost("URL", "数据") - 发送POST请求
            - downloadFile("URL", "本地路径") - 下载文件
            
            ===== OCR识别 =====
            - recognizeText() - 识别当前屏幕文本
            - recognizeTextInRegion(x, y, width, height) - 识别指定区域文本
            
            ===== 日志输出 =====
            - log("日志信息") - 输出日志
            - logInfo("信息") - 输出信息日志
            - logError("错误") - 输出错误日志
            - logWarning("警告") - 输出警告日志
            
            ===== 定时任务 =====
            - scheduleTask(延迟毫秒, 回调函数) - 设置定时任务
            - cancelTask(任务ID) - 取消定时任务
            
            ===== 变量操作 =====
            - setVariable("变量名", "值") - 设置变量
            - getVariable("变量名") - 获取变量值
            - clearVariable("变量名") - 清除变量
            
            ===== 条件控制 =====
            - if (条件) { 代码块 } - 条件判断
            - while (条件) { 代码块 } - 循环执行
            - for (let i = 0; i < 10; i++) { 代码块 } - 计数循环
            
            ===== 异常处理 =====
            - try { 代码块 } catch (e) { 异常处理 } - 异常捕获
            - throw "错误信息" - 抛出异常
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("API 帮助")
            .setMessage(helpText)
            .setPositiveButton("确定", null)
            .show()
    }
    
    private fun formatCode() {
        val content = binding.etScriptCode.text.toString()
        
        try {
            // 简单的代码格式化
            val lines = content.lines()
            val formattedLines = mutableListOf<String>()
            var indentLevel = 0
            
            for (line in lines) {
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty()) {
                    formattedLines.add("")
                    continue
                }
                
                // 减少缩进的情况
                if (trimmedLine.startsWith("}") || 
                    trimmedLine.startsWith("} else") ||
                    trimmedLine.startsWith("} catch") ||
                    trimmedLine.startsWith("} finally")) {
                    indentLevel = maxOf(0, indentLevel - 1)
                }
                
                // 添加缩进
                val indent = "    ".repeat(indentLevel)
                formattedLines.add(indent + trimmedLine)
                
                // 增加缩进的情况
                if (trimmedLine.endsWith("{") ||
                    trimmedLine.startsWith("if ") ||
                    trimmedLine.startsWith("else ") ||
                    trimmedLine.startsWith("try ") ||
                    trimmedLine.startsWith("catch ") ||
                    trimmedLine.startsWith("finally ") ||
                    trimmedLine.startsWith("for ") ||
                    trimmedLine.startsWith("while ") ||
                    trimmedLine.startsWith("function ")) {
                    indentLevel++
                }
            }
            
            val formatted = formattedLines.joinToString("\n")
            binding.etScriptCode.setText(formatted)
            
            // 更新光标位置到末尾
            binding.etScriptCode.setSelection(formatted.length)
            
            Toast.makeText(context, "代码已格式化", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(TAG, "代码格式化失败", e)
            Toast.makeText(context, "代码格式化失败", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateLineInfo() {
        val text = binding.etScriptCode.text.toString()
        val lines = text.lines()
        val currentLine = lines.size
        val currentColumn = if (lines.isNotEmpty()) lines.last().length + 1 else 1
        
        binding.tvLineInfo.text = "第${currentLine}行，第${currentColumn}列"
    }
    
    private fun getDefaultScriptTemplate(): String {
        return """
            // 自动打卡脚本模板
            // 作者：用户
            // 版本：1.0.0
            // 描述：自动执行打卡操作
            
            log("开始执行自动打卡脚本");
            
            try {
                // 启动目标应用
                launchApp("com.example.app");
                sleep(3000);
                
                // 等待应用加载完成
                if (waitForText("首页", 5000)) {
                    log("应用启动成功");
                } else {
                    log("应用启动失败，尝试重新启动");
                    launchApp("com.example.app");
                    sleep(2000);
                }
                
                // 查找并点击打卡按钮
                if (findText("打卡")) {
                    click("打卡");
                    log("点击打卡按钮");
                    sleep(2000);
                    
                    // 检查打卡结果
                    if (findText("打卡成功") || findText("已打卡")) {
                        log("打卡成功！");
                    } else if (findText("打卡失败")) {
                        log("打卡失败，请检查网络或重试");
                    } else {
                        log("打卡状态未知");
                    }
                } else {
                    log("未找到打卡按钮，尝试其他方式");
                    
                    // 尝试通过坐标点击
                    click("200,300");
                    sleep(1000);
                }
                
            } catch (e) {
                logError("脚本执行异常: " + e.message);
            }
            
            log("脚本执行完成");
        """.trimIndent()
    }
    
    private fun getClickTemplate(): String {
        return """
            // 点击操作模板
            log("开始执行点击操作");
            
            try {
                // 点击文本按钮
                if (findText("确定")) {
                    click("确定");
                    log("点击确定按钮");
                    sleep(1000);
                }
                
                // 点击坐标位置
                click("100,200");
                log("点击坐标(100,200)");
                sleep(1000);
                
                // 长按操作
                longClick("长按文本", 2000);
                log("长按操作完成");
                sleep(1000);
                
                // 双击操作
                doubleClick("双击文本");
                log("双击操作完成");
                sleep(1000);
                
                // 检查点击结果
                if (findText("成功")) {
                    log("点击操作成功");
                } else {
                    log("点击操作可能失败");
                }
                
            } catch (e) {
                logError("点击操作异常: " + e.message);
            }
            
            log("点击操作完成");
        """.trimIndent()
    }
    
    private fun getInputTemplate(): String {
        return """
            // 文本输入模板
            log("开始执行文本输入");
            
            try {
                // 清空输入框
                clearText();
                sleep(500);
                
                // 输入文本
                input("要输入的文本内容");
                log("输入文本完成");
                sleep(1000);
                
                // 检查输入结果
                if (findText("要输入的文本内容")) {
                    log("文本输入成功");
                } else {
                    log("文本输入可能失败");
                }
                
                // 点击确定按钮
                if (findText("确定")) {
                    click("确定");
                    log("点击确定按钮");
                    sleep(1000);
                }
                
                // 设置文本内容
                setText("新的文本内容");
                log("设置文本完成");
                
            } catch (e) {
                logError("文本输入异常: " + e.message);
            }
            
            log("文本输入完成");
        """.trimIndent()
    }
    
    private fun getSwipeTemplate(): String {
        return """
            // 滑动操作模板
            log("开始执行滑动操作");
            
            try {
                // 获取屏幕尺寸
                val screenSize = getScreenSize();
                log("屏幕尺寸: " + screenSize);
                
                // 向上滑动
                swipeUp();
                log("向上滑动完成");
                sleep(1000);
                
                // 向下滑动
                swipeDown();
                log("向下滑动完成");
                sleep(1000);
                
                // 向左滑动
                swipeLeft();
                log("向左滑动完成");
                sleep(1000);
                
                // 向右滑动
                swipeRight();
                log("向右滑动完成");
                sleep(1000);
                
                // 自定义滑动
                swipe(200, 500, 200, 100, 1000);
                log("自定义滑动完成");
                sleep(1000);
                
                // 检查滑动结果
                if (findText("新内容")) {
                    log("滑动操作成功，发现新内容");
                } else {
                    log("滑动操作完成");
                }
                
            } catch (e) {
                logError("滑动操作异常: " + e.message);
            }
            
            log("滑动操作完成");
        """.trimIndent()
    }
    
    private fun getLaunchAppTemplate(): String {
        return """
            // 应用启动模板
            log("开始启动应用");
            
            try {
                // 获取当前应用
                val currentApp = getCurrentApp();
                log("当前应用: " + currentApp);
                
                // 启动目标应用
                launchApp("com.example.targetapp");
                log("启动目标应用");
                sleep(3000);
                
                // 等待应用加载完成
                if (waitForText("首页", 5000)) {
                    log("应用启动成功");
                    
                    // 执行应用内操作
                    if (findText("登录")) {
                        click("登录");
                        log("点击登录按钮");
                        sleep(2000);
                    }
                    
                } else {
                    log("应用启动失败，尝试重新启动");
                    launchApp("com.example.targetapp");
                    sleep(2000);
                    
                    if (waitForText("首页", 3000)) {
                        log("重新启动成功");
                    } else {
                        log("应用启动失败");
                        return;
                    }
                }
                
                // 检查应用状态
                if (findText("已登录")) {
                    log("应用已登录，可以执行后续操作");
                } else {
                    log("需要登录操作");
                }
                
            } catch (e) {
                logError("应用启动异常: " + e.message);
            }
            
            log("应用启动操作完成");
        """.trimIndent()
    }
    
    private fun getLoopTemplate(): String {
        return """
            // 循环操作模板
            log("开始执行循环操作");
            
            try {
                // 设置最大重试次数
                const maxRetries = 5;
                let retryCount = 0;
                
                // 循环执行操作
                while (retryCount < maxRetries) {
                    log("执行第" + (retryCount + 1) + "次操作");
                    
                    // 执行具体操作
                    if (findText("按钮")) {
                        click("按钮");
                        log("点击按钮");
                        sleep(1000);
                        
                        // 检查是否成功
                        if (findText("成功")) {
                            log("操作成功，跳出循环");
                            break;
                        } else if (findText("失败")) {
                            log("操作失败，继续重试");
                        } else {
                            log("操作状态未知");
                        }
                    } else {
                        log("未找到目标按钮");
                    }
                    
                    retryCount++;
                    sleep(2000); // 等待2秒后重试
                }
                
                // 检查最终结果
                if (retryCount >= maxRetries) {
                    log("达到最大重试次数，操作失败");
                } else {
                    log("循环操作成功完成");
                }
                
                // 计数循环示例
                for (let i = 0; i < 3; i++) {
                    log("执行第" + (i + 1) + "次计数操作");
                    swipeUp();
                    sleep(1000);
                }
                
            } catch (e) {
                logError("循环操作异常: " + e.message);
            }
            
            log("循环操作完成");
        """.trimIndent()
    }
    
    private fun getConditionTemplate(): String {
        return """
            // 条件判断模板
            log("开始执行条件判断");
            
            // 检查条件
            if (findText("条件文本")) {
                log("条件满足");
            } else {
                log("条件不满足");
            }
            
            log("条件判断完成");
        """.trimIndent()
    }
    
    private fun getFileOperationTemplate(): String {
        return """
            // 文件操作模板
            log("开始执行文件操作");
            
            // 读取文件内容
            val content = readFile("path/to/file.txt");
            log("文件内容：" + content);
            
            // 写入文件
            writeFile("path/to/file.txt", "新内容");
            
            log("文件操作完成");
        """.trimIndent()
    }
    
    private fun getNetworkTemplate(): String {
        return """
            // 网络请求模板
            log("开始执行网络请求");
            
            // 发送网络请求
            val response = sendNetworkRequest("https://api.example.com/data");
            log("响应内容：" + response);
            
            log("网络请求完成");
        """.trimIndent()
    }
    
    private fun getOcrTemplate(): String {
        return """
            // OCR识别模板
            log("开始执行OCR识别");
            
            // 获取屏幕截图
            val screenshot = getScreenShot();
            
            // 识别文本
            val text = recognizeText(screenshot);
            log("识别结果：" + text);
            
            log("OCR识别完成");
        """.trimIndent()
    }
    
    private fun getScheduledTaskTemplate(): String {
        return """
            // 定时任务模板
            log("开始执行定时任务");
            
            // 设置定时任务
            scheduleTask(10000) {
                log("定时任务执行");
            }
            
            log("定时任务设置完成");
        """.trimIndent()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 