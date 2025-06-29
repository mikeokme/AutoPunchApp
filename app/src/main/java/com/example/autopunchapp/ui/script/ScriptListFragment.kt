package com.example.autopunchapp.ui.script

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.autopunchapp.R
import com.example.autopunchapp.databinding.FragmentScriptListBinding
import com.example.autopunchapp.model.Script
import com.example.autopunchapp.model.ScriptCategory
import com.example.autopunchapp.script.ScriptManager
import kotlinx.coroutines.launch

class ScriptListFragment : Fragment() {
    
    companion object {
        private const val TAG = "ScriptListFragment"
        
        fun newInstance(): ScriptListFragment {
            return ScriptListFragment()
        }
    }
    
    private var _binding: FragmentScriptListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var scriptManager: ScriptManager
    private lateinit var scriptAdapter: ScriptAdapter
    private var allScripts = listOf<Script>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScriptListBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        scriptManager = ScriptManager.getInstance(requireContext())
        
        setupViews()
        setupListeners()
        loadScripts()
    }
    
    private fun setupViews() {
        // 设置RecyclerView
        scriptAdapter = ScriptAdapter(
            onScriptClick = { script -> openScriptEditor(script) },
            onScriptRun = { script -> runScript(script) },
            onScriptEdit = { script -> openScriptEditor(script) },
            onScriptDelete = { script -> deleteScript(script) },
            onScriptShare = { script -> shareScript(script) }
        )
        
        binding.recyclerViewScripts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = scriptAdapter
        }
        
        // 设置搜索框
        binding.searchViewScripts.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterScripts(query)
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                filterScripts(newText)
                return true
            }
        })
        
        // 设置分类过滤器
        setupCategoryFilter()
    }
    
    private fun setupListeners() {
        // 新建脚本按钮
        binding.fabAddScript.setOnClickListener {
            openScriptEditor(null)
        }
        
        // 导入脚本按钮
        binding.btnImportScript.setOnClickListener {
            importScript()
        }
        
        // 导出脚本按钮
        binding.btnExportScript.setOnClickListener {
            exportScripts()
        }
        
        // 刷新按钮
        binding.btnRefresh.setOnClickListener {
            loadScripts()
        }
    }
    
    private fun setupCategoryFilter() {
        val categories = arrayOf("全部", "打卡", "游戏", "社交", "购物", "工具", "教育", "金融", "其他")
        
        binding.spinnerCategory.apply {
            adapter = android.widget.ArrayAdapter(context, android.R.layout.simple_spinner_item, categories)
            setSelection(0)
            
            setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    filterByCategory(position)
                }
                
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            })
        }
    }
    
    private fun loadScripts() {
        lifecycleScope.launch {
            try {
                allScripts = scriptManager.getAllScripts()
                updateScriptList(allScripts)
                
                if (allScripts.isEmpty()) {
                    showEmptyState()
                } else {
                    hideEmptyState()
                }
                
            } catch (e: Exception) {
                Toast.makeText(context, "加载脚本失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateScriptList(scripts: List<Script>) {
        scriptAdapter.updateScripts(scripts)
        binding.tvScriptCount.text = "共 ${scripts.size} 个脚本"
    }
    
    private fun filterScripts(query: String?) {
        if (query.isNullOrBlank()) {
            updateScriptList(allScripts)
            return
        }
        
        val filtered = allScripts.filter { script ->
            script.name.contains(query, ignoreCase = true) ||
            script.description.contains(query, ignoreCase = true) ||
            script.author.contains(query, ignoreCase = true) ||
            script.content.contains(query, ignoreCase = true)
        }
        
        updateScriptList(filtered)
    }
    
    private fun filterByCategory(position: Int) {
        if (position == 0) {
            updateScriptList(allScripts)
            return
        }
        val category = when (position) {
            1 -> com.example.autopunchapp.model.ScriptCategory.PUNCH_IN
            2 -> com.example.autopunchapp.model.ScriptCategory.GAME
            3 -> com.example.autopunchapp.model.ScriptCategory.SOCIAL
            4 -> com.example.autopunchapp.model.ScriptCategory.SHOPPING
            5 -> com.example.autopunchapp.model.ScriptCategory.TOOL
            6 -> com.example.autopunchapp.model.ScriptCategory.EDUCATION
            7 -> com.example.autopunchapp.model.ScriptCategory.FINANCE
            8 -> com.example.autopunchapp.model.ScriptCategory.OTHER
            else -> com.example.autopunchapp.model.ScriptCategory.OTHER
        }
        val filtered = allScripts.filter { it.category == category }
        updateScriptList(filtered)
    }
    
    private fun openScriptEditor(script: Script?) {
        val fragment = ScriptEditorFragment.newInstance(script?.id)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_content_main, fragment)
            .addToBackStack(null)
            .commit()
    }
    
    private fun runScript(script: Script) {
        lifecycleScope.launch {
            try {
                val result = scriptManager.executeScript(script.content)
                
                val message = if (result.success) {
                    "脚本执行成功: ${result.message}"
                } else {
                    "脚本执行失败: ${result.message}"
                }
                
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                
            } catch (e: Exception) {
                Toast.makeText(context, "脚本执行异常: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun deleteScript(script: Script) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("删除脚本")
            .setMessage("确定要删除脚本 '${script.name}' 吗？")
            .setPositiveButton("删除") { _, _ ->
                lifecycleScope.launch {
                    val success = scriptManager.deleteScript(script.id)
                    if (success) {
                        Toast.makeText(context, "脚本删除成功", Toast.LENGTH_SHORT).show()
                        loadScripts()
                    } else {
                        Toast.makeText(context, "脚本删除失败", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun shareScript(script: Script) {
        val shareText = """
            脚本名称: ${script.name}
            作者: ${script.author}
            版本: ${script.version}
            描述: ${script.description}
            
            脚本内容:
            ${script.content}
        """.trimIndent()
        
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, shareText)
            putExtra(android.content.Intent.EXTRA_SUBJECT, "分享脚本: ${script.name}")
        }
        
        startActivity(android.content.Intent.createChooser(intent, "分享脚本"))
    }
    
    private fun importScript() {
        // 这里可以实现从文件导入脚本的功能
        Toast.makeText(context, "导入脚本功能开发中", Toast.LENGTH_SHORT).show()
    }
    
    private fun exportScripts() {
        // 这里可以实现导出脚本的功能
        Toast.makeText(context, "导出脚本功能开发中", Toast.LENGTH_SHORT).show()
    }
    
    private fun showEmptyState() {
        binding.layoutEmpty.visibility = View.VISIBLE
        binding.recyclerViewScripts.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        binding.layoutEmpty.visibility = View.GONE
        binding.recyclerViewScripts.visibility = View.VISIBLE
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 