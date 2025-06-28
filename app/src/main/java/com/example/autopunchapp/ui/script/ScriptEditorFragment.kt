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
            requireActivity().onBackPressed()
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
                requireActivity().onBackPressed()
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
            "循环操作模板"
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
                    else -> getDefaultScriptTemplate()
                }
                binding.etScriptCode.setText(template)
            }
            .show()
    }
    
    private fun showApiHelpDialog() {
        val helpText = """
            API 帮助文档：
            
            点击操作：
            - click("按钮文本") - 点击包含指定文本的按钮
            - click("100,200") - 点击指定坐标
            
            文本输入：
            - input("要输入的文本") - 在当前焦点输入文本
            
            滑动操作：
            - swipe(100, 200, 300, 400, 500) - 从(100,200)滑动到(300,400)，持续500ms
            
            等待操作：
            - sleep(1000) - 等待1000毫秒
            
            查找元素：
            - findText("文本") - 查找包含指定文本的元素
            
            启动应用：
            - launchApp("com.example.app") - 启动指定应用
            
            日志输出：
            - log("日志信息") - 输出日志
            
            获取屏幕信息：
            - getScreenSize() - 获取屏幕尺寸
        """.trimIndent()
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("API 帮助")
            .setMessage(helpText)
            .setPositiveButton("确定", null)
            .show()
    }
    
    private fun formatCode() {
        val content = binding.etScriptCode.text.toString()
        // 这里可以实现简单的代码格式化
        // 目前只是简单的缩进处理
        val formatted = content.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString("\n") { "    $it" }
        
        binding.etScriptCode.setText(formatted)
        Toast.makeText(context, "代码已格式化", Toast.LENGTH_SHORT).show()
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
            
            log("开始执行自动打卡脚本");
            
            // 启动目标应用
            launchApp("com.example.app");
            sleep(2000);
            
            // 查找并点击打卡按钮
            if (findText("打卡")) {
                click("打卡");
                log("打卡成功");
            } else {
                log("未找到打卡按钮");
            }
            
            log("脚本执行完成");
        """.trimIndent()
    }
    
    private fun getClickTemplate(): String {
        return """
            // 点击操作模板
            log("开始执行点击操作");
            
            // 点击文本按钮
            click("确定");
            sleep(1000);
            
            // 点击坐标位置
            click("100,200");
            sleep(1000);
            
            log("点击操作完成");
        """.trimIndent()
    }
    
    private fun getInputTemplate(): String {
        return """
            // 文本输入模板
            log("开始执行文本输入");
            
            // 输入文本
            input("要输入的文本内容");
            sleep(1000);
            
            // 点击确定按钮
            click("确定");
            
            log("文本输入完成");
        """.trimIndent()
    }
    
    private fun getSwipeTemplate(): String {
        return """
            // 滑动操作模板
            log("开始执行滑动操作");
            
            // 向上滑动
            swipe(200, 500, 200, 100, 1000);
            sleep(1000);
            
            // 向下滑动
            swipe(200, 100, 200, 500, 1000);
            sleep(1000);
            
            log("滑动操作完成");
        """.trimIndent()
    }
    
    private fun getLaunchAppTemplate(): String {
        return """
            // 应用启动模板
            log("开始启动应用");
            
            // 启动应用
            launchApp("com.example.targetapp");
            sleep(3000);
            
            // 等待应用加载完成
            if (findText("首页")) {
                log("应用启动成功");
            } else {
                log("应用启动失败");
            }
        """.trimIndent()
    }
    
    private fun getLoopTemplate(): String {
        return """
            // 循环操作模板
            log("开始执行循环操作");
            
            // 循环执行5次
            for (let i = 0; i < 5; i++) {
                log("执行第" + (i + 1) + "次操作");
                
                // 执行具体操作
                click("按钮");
                sleep(1000);
                
                // 检查是否成功
                if (findText("成功")) {
                    log("操作成功");
                    break;
                }
            }
            
            log("循环操作完成");
        """.trimIndent()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 