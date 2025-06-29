package com.example.autopunchapp.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.autopunchapp.R
import com.example.autopunchapp.databinding.FragmentHomeBinding
import com.example.autopunchapp.model.AppInfo
import com.example.autopunchapp.model.PunchLog
import com.example.autopunchapp.ui.recordaction.RecordActionFragment
import com.example.autopunchapp.ui.script.ScriptEditorFragment
import com.example.autopunchapp.ui.timesetting.TimeSettingFragment
import com.example.autopunchapp.utils.AccessibilityUtil
import com.example.autopunchapp.utils.AppListUtil
import com.example.autopunchapp.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var punchLogAdapter: PunchLogAdapter
    private lateinit var preferenceManager: PreferenceManager
    private var selectedApp: AppInfo? = null

    companion object {
        private const val TAG = "HomeFragment"
        private const val REQUEST_QUERY_ALL_PACKAGES = 1001
    }

    // 权限请求回调
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showAppSelectionDialog()
        } else {
            Toast.makeText(requireContext(), "需要权限才能选择已安装的应用", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            Log.d(TAG, "onCreateView: Creating HomeFragment view")
            _binding = FragmentHomeBinding.inflate(inflater, container, false)
            return binding.root
        } catch (e: Exception) {
            Log.e(TAG, "onCreateView: Error creating view", e)
            throw e
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        try {
            Log.d(TAG, "onViewCreated: Setting up HomeFragment")
            preferenceManager = PreferenceManager(requireContext())

            setupUI()
            setupListeners()
            loadData()
            Log.d(TAG, "onViewCreated: HomeFragment setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated: Error setting up HomeFragment", e)
            Toast.makeText(requireContext(), "页面初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUI() {
        try {
            // 设置打卡软件下拉框
            setupSpinner()

            // 设置打卡日志列表
            binding.rvPunchLog.layoutManager = LinearLayoutManager(requireContext())
            punchLogAdapter = PunchLogAdapter(mutableListOf())
            binding.rvPunchLog.adapter = punchLogAdapter

            // 更新UI状态
            updateUIState()
        } catch (e: Exception) {
            Log.e(TAG, "setupUI: Error setting up UI", e)
            Toast.makeText(requireContext(), "界面设置失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        try {
            // 开始打卡按钮
            binding.btnStartPunch.setOnClickListener {
                try {
                    if (!AccessibilityUtil.isAccessibilityServiceEnabled(requireContext())) {
                        Toast.makeText(requireContext(), "请先开启无障碍服务", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (selectedApp == null) {
                        Toast.makeText(requireContext(), "请先选择打卡软件", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    if (binding.tvRecordStatus.text == getString(R.string.status_not_recorded)) {
                        Toast.makeText(requireContext(), "请先录制打卡动作", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    // 执行打卡操作
                    Toast.makeText(requireContext(), "开始执行打卡操作", Toast.LENGTH_SHORT).show()
                    // TODO: 实现打卡操作逻辑
                    
                    // 模拟打卡成功
                    val currentTime = Calendar.getInstance().time
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    
                    val newLog = PunchLog(
                        dateFormat.format(currentTime),
                        timeFormat.format(currentTime),
                        true
                    )
                    
                    punchLogAdapter.addLog(newLog)
                    preferenceManager.savePunchLog(newLog)
                    preferenceManager.saveLastPunchTime(timeFormat.format(currentTime))
                    preferenceManager.savePunchStatus(true)
                    
                    // 更新UI状态
                    binding.tvPunchStatus.text = getString(R.string.status_completed)
                    binding.tvPunchStatus.setBackgroundResource(R.drawable.status_completed_background)
                    binding.tvPunchTime.text = "上次打卡时间: ${timeFormat.format(currentTime)}"
                } catch (e: Exception) {
                    Log.e(TAG, "btnStartPunch onClick: Error", e)
                    Toast.makeText(requireContext(), "打卡操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // 设置时间段按钮
            binding.btnSetTime.setOnClickListener {
                try {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_content_main, TimeSettingFragment())
                        .addToBackStack(null)
                        .commit()
                } catch (e: Exception) {
                    Log.e(TAG, "btnSetTime onClick: Error", e)
                    Toast.makeText(requireContext(), "页面跳转失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // 录制动作按钮
            binding.btnRecordAction.setOnClickListener {
                try {
                    if (!AccessibilityUtil.isAccessibilityServiceEnabled(requireContext())) {
                        Toast.makeText(requireContext(), "请先开启无障碍服务", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_content_main, RecordActionFragment())
                        .addToBackStack(null)
                        .commit()
                } catch (e: Exception) {
                    Log.e(TAG, "btnRecordAction onClick: Error", e)
                    Toast.makeText(requireContext(), "页面跳转失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // 脚本编辑器按钮
            binding.btnScriptEditor.setOnClickListener {
                try {
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment_content_main, ScriptEditorFragment.newInstance())
                        .addToBackStack(null)
                        .commit()
                } catch (e: Exception) {
                    Log.e(TAG, "btnScriptEditor onClick: Error", e)
                    Toast.makeText(requireContext(), "页面跳转失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // 脚本市场按钮
            binding.btnScriptMarket.setOnClickListener {
                try {
                    Toast.makeText(requireContext(), "脚本市场功能开发中...", Toast.LENGTH_SHORT).show()
                    // TODO: 实现脚本市场功能
                } catch (e: Exception) {
                    Log.e(TAG, "btnScriptMarket onClick: Error", e)
                    Toast.makeText(requireContext(), "功能调用失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // 我的脚本按钮
            binding.btnMyScripts.setOnClickListener {
                try {
                    Toast.makeText(requireContext(), "我的脚本功能开发中...", Toast.LENGTH_SHORT).show()
                    // TODO: 实现我的脚本功能
                } catch (e: Exception) {
                    Log.e(TAG, "btnMyScripts onClick: Error", e)
                    Toast.makeText(requireContext(), "功能调用失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // 执行日志按钮
            binding.btnScriptLogs.setOnClickListener {
                try {
                    Toast.makeText(requireContext(), "执行日志功能开发中...", Toast.LENGTH_SHORT).show()
                    // TODO: 实现执行日志功能
                } catch (e: Exception) {
                    Log.e(TAG, "btnScriptLogs onClick: Error", e)
                    Toast.makeText(requireContext(), "功能调用失败: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupListeners: Error setting up listeners", e)
            Toast.makeText(requireContext(), "事件设置失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadData() {
        try {
            // 加载保存的打卡状态
            val isPunchCompleted = preferenceManager.getPunchStatus()
            val lastPunchTime = preferenceManager.getLastPunchTime()
            val selectedAppPackageName = preferenceManager.getSelectedApp(requireContext())
            val isActionRecorded = preferenceManager.getActionRecordStatus()

            // 更新UI
            if (isPunchCompleted) {
                binding.tvPunchStatus.text = getString(R.string.status_completed)
                binding.tvPunchStatus.setBackgroundResource(R.drawable.status_completed_background)
            } else {
                binding.tvPunchStatus.text = getString(R.string.status_pending)
                binding.tvPunchStatus.setBackgroundResource(R.drawable.status_pending_background)
            }

            if (lastPunchTime.isNotEmpty()) {
                binding.tvPunchTime.text = "上次打卡时间: $lastPunchTime"
            }

            // 恢复选择的应用
            if (selectedAppPackageName.isNotEmpty()) {
                val savedIndex = when (selectedAppPackageName) {
                    "com.alibaba.android.rimet" -> {
                        selectedApp = AppInfo("com.alibaba.android.rimet", "钉钉", requireContext().getDrawable(android.R.drawable.ic_menu_help)!!)
                        0
                    }
                    "com.tencent.wework" -> {
                        selectedApp = AppInfo("com.tencent.wework", "企业微信", requireContext().getDrawable(android.R.drawable.ic_menu_help)!!)
                        1
                    }
                    "com.ss.android.lark" -> {
                        selectedApp = AppInfo("com.ss.android.lark", "飞书", requireContext().getDrawable(android.R.drawable.ic_menu_help)!!)
                        2
                    }
                    "com.mobileoffice.m3" -> {
                        selectedApp = AppInfo("com.mobileoffice.m3", "移动办公M3", requireContext().getDrawable(android.R.drawable.ic_menu_help)!!)
                        3
                    }
                    else -> {
                        // 对于其他应用，需要从已安装应用列表中查找
                        val installedApps = AppListUtil.getInstalledApps(requireContext())
                        val foundApp = installedApps.find { it.packageName == selectedAppPackageName }
                        if (foundApp != null) {
                            selectedApp = foundApp
                        }
                        4
                    }
                }
                binding.spinnerAppSelection.setSelection(savedIndex)
            }

            if (isActionRecorded) {
                binding.tvRecordStatus.text = getString(R.string.status_recorded)
                binding.tvRecordStatus.setBackgroundResource(R.drawable.status_completed_background)
            } else {
                binding.tvRecordStatus.text = getString(R.string.status_not_recorded)
                binding.tvRecordStatus.setBackgroundResource(R.drawable.status_pending_background)
            }

            // 加载打卡日志
            val logs = preferenceManager.getPunchLogs()
            punchLogAdapter.updateLogs(logs)
        } catch (e: Exception) {
            Log.e(TAG, "loadData: Error loading data", e)
            Toast.makeText(requireContext(), "数据加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUIState() {
        try {
            // 根据当前状态更新UI
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            
            // 假设工作时间为9:00-18:00，在此时间段外自动重置打卡状态为待打卡
            if (currentHour < 9 || currentHour >= 18) {
                binding.tvPunchStatus.text = getString(R.string.status_pending)
                binding.tvPunchStatus.setBackgroundResource(R.drawable.status_pending_background)
                preferenceManager.savePunchStatus(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateUIState: Error updating UI state", e)
        }
    }

    private fun showAppSelectionDialog() {
        try {
            val installedApps = AppListUtil.getInstalledApps(requireContext())
            
            if (installedApps.isEmpty()) {
                Toast.makeText(requireContext(), "未找到已安装的应用", Toast.LENGTH_SHORT).show()
                return
            }
            
            val appNames = installedApps.map { it.appName }.toTypedArray()
            
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("选择打卡软件")
                .setItems(appNames) { _, which ->
                    val selectedAppInfo = installedApps[which]
                    selectedApp = selectedAppInfo
                    preferenceManager.saveSelectedApp(requireContext(), selectedAppInfo.packageName)
                    
                    // 更新spinner显示
                    binding.spinnerAppSelection.setSelection(4) // 显示"其他软件"
                    
                    Toast.makeText(
                        requireContext(), 
                        "已选择: ${selectedAppInfo.appName}", 
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    Log.d(TAG, "User selected app: ${selectedAppInfo.appName} (${selectedAppInfo.packageName})")
                }
                .setNegativeButton("取消") { _, _ ->
                    // 恢复之前的选择
                    val savedPackageName = preferenceManager.getSelectedApp(requireContext())
                    if (savedPackageName.isNotEmpty()) {
                        val savedIndex = when (savedPackageName) {
                            "com.alibaba.android.rimet" -> 0
                            "com.tencent.wework" -> 1
                            "com.ss.android.lark" -> 2
                            "com.mobileoffice.m3" -> 3
                            else -> 4
                        }
                        binding.spinnerAppSelection.setSelection(savedIndex)
                    }
                }
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "showAppSelectionDialog: Error", e)
            Toast.makeText(requireContext(), "显示应用选择对话框失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinner() {
        try {
            val appOptions = mutableListOf("钉钉", "企业微信", "飞书", "移动办公M3", "其他软件...")
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                appOptions
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.spinnerAppSelection.adapter = adapter
            // 设置选择监听器
            binding.spinnerAppSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    when (position) {
                        0 -> {
                            selectedApp = AppInfo("com.alibaba.android.rimet", "钉钉", requireContext().getDrawable(android.R.drawable.ic_menu_help)!!)
                            preferenceManager.saveSelectedApp(requireContext(), selectedApp!!.packageName)
                            Log.d(TAG, "Selected app: ${selectedApp!!.appName} (${selectedApp!!.packageName})")
                        }
                        1 -> {
                            selectedApp = AppInfo("com.tencent.wework", "企业微信", requireContext().getDrawable(android.R.drawable.ic_menu_help)!!)
                            preferenceManager.saveSelectedApp(requireContext(), selectedApp!!.packageName)
                            Log.d(TAG, "Selected app: ${selectedApp!!.appName} (${selectedApp!!.packageName})")
                        }
                        2 -> {
                            selectedApp = AppInfo("com.ss.android.lark", "飞书", requireContext().getDrawable(android.R.drawable.ic_menu_help)!!)
                            preferenceManager.saveSelectedApp(requireContext(), selectedApp!!.packageName)
                            Log.d(TAG, "Selected app: ${selectedApp!!.appName} (${selectedApp!!.packageName})")
                        }
                        3 -> {
                            selectedApp = AppInfo("com.mobileoffice.m3", "移动办公M3", requireContext().getDrawable(android.R.drawable.ic_menu_help)!!)
                            preferenceManager.saveSelectedApp(requireContext(), selectedApp!!.packageName)
                            Log.d(TAG, "Selected app: ${selectedApp!!.appName} (${selectedApp!!.packageName})")
                        }
                        4 -> {
                            // 对于"其他软件..."，不立即设置selectedApp，而是显示选择对话框
                            checkPermissionAndShowAppSelection()
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
            // 恢复之前选择的应用
            val savedPackageName = preferenceManager.getSelectedApp(requireContext())
            val savedIndex = when (savedPackageName) {
                "com.alibaba.android.rimet" -> 0
                "com.tencent.wework" -> 1
                "com.ss.android.lark" -> 2
                "com.mobileoffice.m3" -> 3
                else -> 4
            }
            binding.spinnerAppSelection.setSelection(savedIndex)
        } catch (e: Exception) {
            Log.e(TAG, "setupSpinner: Error", e)
            Toast.makeText(context, "软件选择设置失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionAndShowAppSelection() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.QUERY_ALL_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.QUERY_ALL_PACKAGES)
        } else {
            showAppSelectionDialog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}