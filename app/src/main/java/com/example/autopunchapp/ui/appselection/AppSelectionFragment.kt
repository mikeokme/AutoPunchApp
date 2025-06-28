package com.example.autopunchapp.ui.appselection

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.autopunchapp.R
import com.example.autopunchapp.databinding.FragmentAppSelectionBinding
import com.example.autopunchapp.model.AppInfo
import com.example.autopunchapp.utils.PreferenceManager

class AppSelectionFragment : Fragment() {
    
    private var _binding: FragmentAppSelectionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var appAdapter: AppAdapter
    private val appList = mutableListOf<AppInfo>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferenceManager = PreferenceManager(requireContext())
        
        setupRecyclerView()
        loadInstalledApps()
        setupListeners()
    }
    
    private fun setupRecyclerView() {
        appAdapter = AppAdapter(appList) { appInfo, isSelected ->
            // 处理应用选择状态变化
            appInfo.isSelected = isSelected
        }
        
        binding.rvAppList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = appAdapter
        }
    }
    
    private fun loadInstalledApps() {
        val packageManager = requireContext().packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        
        appList.clear()
        
        // 过滤出用户应用（排除系统应用）
        val userApps = installedApps.filter { 
            it.flags and ApplicationInfo.FLAG_SYSTEM == 0 
        }
        
        // 添加一些常见的打卡应用
        val commonApps = listOf(
            "com.tencent.wework", // 企业微信
            "com.alibaba.android.rimet", // 钉钉
            "com.tencent.mm", // 微信
            "com.tencent.tim", // QQ
            "com.netease.cloudmusic", // 网易云音乐
            "com.tencent.mobileqq" // QQ
        )
        
        for (app in userApps) {
            try {
                val appName = packageManager.getApplicationLabel(app).toString()
                val appIcon = packageManager.getApplicationIcon(app.packageName)
                
                val appInfo = AppInfo(
                    packageName = app.packageName,
                    appName = appName,
                    appIcon = appIcon,
                    isSelected = false
                )
                
                appList.add(appInfo)
            } catch (e: Exception) {
                // 忽略无法获取信息的应用
            }
        }
        
        // 添加常见应用（如果已安装）
        for (packageName in commonApps) {
            try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val appName = packageManager.getApplicationLabel(appInfo).toString()
                val appIcon = packageManager.getApplicationIcon(packageName)
                
                val app = AppInfo(
                    packageName = packageName,
                    appName = appName,
                    appIcon = appIcon,
                    isSelected = false
                )
                
                // 避免重复添加
                if (!appList.any { it.packageName == packageName }) {
                    appList.add(0, app) // 添加到列表开头
                }
            } catch (e: Exception) {
                // 应用未安装，忽略
            }
        }
        
        appAdapter.notifyDataSetChanged()
    }
    
    private fun setupListeners() {
        // 搜索功能
        binding.etSearchApp.setOnEditorActionListener { _, _, _ ->
            filterApps(binding.etSearchApp.text.toString())
            true
        }
        
        // 确认按钮
        binding.btnConfirm.setOnClickListener {
            val selectedApps = appList.filter { it.isSelected }
            if (selectedApps.isEmpty()) {
                Toast.makeText(requireContext(), "请至少选择一个应用", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 保存选中的应用位置
            val selectedIndex = appList.indexOf(selectedApps.first())
            preferenceManager.saveSelectedApp(selectedIndex)
            Toast.makeText(requireContext(), "应用选择已保存", Toast.LENGTH_SHORT).show()
            
            // 返回上一页
            parentFragmentManager.popBackStack()
        }
        
        // 取消按钮
        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun filterApps(query: String) {
        if (query.isEmpty()) {
            appAdapter.updateApps(appList)
        } else {
            val filteredList = appList.filter { 
                it.appName.contains(query, ignoreCase = true) ||
                it.packageName.contains(query, ignoreCase = true)
            }
            appAdapter.updateApps(filteredList)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 