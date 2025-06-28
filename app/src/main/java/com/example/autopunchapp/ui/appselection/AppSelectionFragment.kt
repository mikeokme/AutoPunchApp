package com.example.autopunchapp.ui.appselection

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.autopunchapp.databinding.FragmentAppSelectionBinding
import com.example.autopunchapp.model.AppInfo
import com.example.autopunchapp.utils.AppListUtil
import com.example.autopunchapp.utils.PreferenceManager

class AppSelectionFragment : Fragment() {
    
    private var _binding: FragmentAppSelectionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var appAdapter: AppAdapter
    private val appList = mutableListOf<AppInfo>()
    
    private var selectedApp: AppInfo? = null

    companion object {
        private const val TAG = "AppSelectionFragment"
    }

    // 权限请求回调
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showInstalledAppsDialog()
        } else {
            Toast.makeText(context, "需要权限才能选择已安装的应用", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            Log.d(TAG, "onViewCreated: Initializing AppSelectionFragment")
            
            preferenceManager = PreferenceManager(requireContext())
            
            setupRecyclerView()
            loadInstalledApps()
            loadCommonApps()
            setupListeners()
            
            Log.d(TAG, "onViewCreated: AppSelectionFragment initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated: Error initializing AppSelectionFragment", e)
            Toast.makeText(context, "页面初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
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
    
    @SuppressLint("NotifyDataSetChanged")
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
            "com.tencent.mobileqq", // QQ
            "com.m3.mobileoffice" // 移动办公M3
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
    
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadCommonApps() {
        try {
            val commonApps = listOf(
                AppInfo("com.alibaba.android.rimet", "钉钉", requireContext().getDrawable(android.R.drawable.ic_menu_help)!!),
                AppInfo("com.tencent.wework", "企业微信", requireContext().getDrawable(android.R.drawable.ic_menu_help)!!),
                AppInfo("com.ss.android.lark", "飞书", requireContext().getDrawable(android.R.drawable.ic_menu_help)!!),
                AppInfo("com.mobileoffice.m3", "移动办公M3", requireContext().getDrawable(android.R.drawable.ic_menu_help)!!)
            )
            appAdapter.updateApps(commonApps)
        } catch (e: Exception) {
            Log.e(TAG, "loadCommonApps: Error", e)
            Toast.makeText(context, "加载常用应用失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.R)
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
            
            // 保存选中的应用包名
            val selectedApp = selectedApps.first()
            PreferenceManager.saveSelectedApp(selectedApp.packageName)
            Toast.makeText(requireContext(), "应用选择已保存", Toast.LENGTH_SHORT).show()
            
            // 返回上一页
            parentFragmentManager.popBackStack()
        }
        
        // 取消按钮
        binding.btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // 选择其他应用按钮
        binding.btnSelectOtherApp.setOnClickListener {
            checkPermissionAndShowAppSelection()
        }

        // 检查无障碍服务按钮
        binding.btnCheckAccessibility.setOnClickListener {
            checkAccessibilityService()
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

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkPermissionAndShowAppSelection() {
        try {
            if (AppListUtil.hasQueryAllPackagesPermission(requireContext())) {
                showInstalledAppsDialog()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.QUERY_ALL_PACKAGES)
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkPermissionAndShowAppSelection: Error", e)
            Toast.makeText(context, "权限检查失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showInstalledAppsDialog() {
        try {
            val installedApps = AppListUtil.getInstalledApps(requireContext())
            
            if (installedApps.isEmpty()) {
                Toast.makeText(context, "未找到已安装的应用", Toast.LENGTH_SHORT).show()
                return
            }
            
            val appNames = installedApps.map { it.appName }.toTypedArray()
            
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("选择已安装的应用")
                .setItems(appNames) { _, which ->
                    val selectedAppInfo = installedApps[which]
                    selectedApp = selectedAppInfo
                    PreferenceManager.saveSelectedApp(selectedAppInfo.packageName)
                    
                    Toast.makeText(
                        context, 
                        "已选择: ${selectedAppInfo.appName}", 
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    Log.d(TAG, "User selected installed app: ${selectedAppInfo.appName} (${selectedAppInfo.packageName})")
                }
                .setNegativeButton("取消", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "showInstalledAppsDialog: Error", e)
            Toast.makeText(context, "显示应用选择对话框失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkAccessibilityService() {
        try {
            if (isAccessibilityServiceEnabled()) {
                Toast.makeText(context, "无障碍服务已开启", Toast.LENGTH_SHORT).show()
            } else {
                showAccessibilityServiceDialog()
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkAccessibilityService: Error", e)
            Toast.makeText(context, "检查无障碍服务失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        return try {
            val accessibilityEnabled = Settings.Secure.getInt(
                requireContext().contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
            
            if (accessibilityEnabled == 1) {
                val serviceString = Settings.Secure.getString(
                    requireContext().contentResolver,
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
                )
                serviceString?.contains("com.example.autopunchapp/.service.AutoPunchAccessibilityService") == true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "isAccessibilityServiceEnabled: Error", e)
            false
        }
    }

    private fun showAccessibilityServiceDialog() {
        try {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("需要开启无障碍服务")
                .setMessage("自动打卡功能需要无障碍服务支持，请前往系统设置开启。")
                .setPositiveButton("前往设置") { _, _ ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("取消", null)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "showAccessibilityServiceDialog: Error", e)
            Toast.makeText(context, "对话框显示失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 