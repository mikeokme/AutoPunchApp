package com.example.autopunchapp.ui.recordaction

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.autopunchapp.R
import com.example.autopunchapp.databinding.FragmentRecordActionBinding
import com.example.autopunchapp.model.Action
import com.example.autopunchapp.service.AutoPunchAccessibilityService
import com.example.autopunchapp.utils.AccessibilityUtil
import com.example.autopunchapp.utils.PreferenceManager

class RecordActionFragment : Fragment() {
    
    private var _binding: FragmentRecordActionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    
    private val recordedActions = mutableListOf<Action>()
    private var isRecording = false
    private var recordingStartTime = 0L
    private var selectedAppPackageName = ""
    
    // 新增：权限请求Launcher
    private lateinit var permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
    
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        private const val OVERLAY_PERMISSION_REQUEST_CODE = 1002
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecordActionBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferenceManager = PreferenceManager(requireContext())
        windowManager = requireContext().getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
        
        // 注册权限请求Launcher
        permissionLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            val deniedPermissions = result.filterValues { !it }.keys
            if (deniedPermissions.isNotEmpty()) {
                // 权限被拒绝，显示详细说明
                showPermissionDeniedDialog(deniedPermissions.toList())
            } else {
                // 权限全部通过，继续检查其他权限
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    checkAndRequestPermissions(skipPermissionRequest = true)
                }
            }
        }
        
        setupUI()
        setupListeners()
    }
    
    private fun setupUI() {
        // 使用统一的权限检查方法
        updatePermissionStatus()
    }
    
    private fun setupListeners() {
        // 开始录制按钮
        binding.btnStartRecord.setOnClickListener {
            if (!AccessibilityUtil.isAccessibilityServiceEnabled(requireContext())) {
                showAccessibilityServiceDialog()
                return@setOnClickListener
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkAndRequestPermissions()
            }
        }
        
        // 暂停录制按钮
        binding.btnPauseRecord.setOnClickListener {
            pauseRecording()
        }
        
        // 停止录制按钮
        binding.btnStopRecord.setOnClickListener {
            stopRecording()
        }
        
        // 取消录制按钮
        binding.btnCancelRecord.setOnClickListener {
            cancelRecording()
        }
        
        // 保存录制按钮
        binding.btnSaveRecord.setOnClickListener {
            saveRecording()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkAndRequestPermissions(skipPermissionRequest: Boolean = false) {
        // 1. 首先检查无障碍服务
        if (!AccessibilityUtil.isAccessibilityServiceEnabled(requireContext())) {
            showAccessibilityServiceDialog()
            return
        }
        
        // 2. 检查应用选择
        val selectedAppPackageName = preferenceManager.getSelectedApp(requireContext())
        if (selectedAppPackageName.isEmpty()) {
            Toast.makeText(requireContext(), "请先选择打卡应用", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 3. 检查启动其他应用的权限
        if (!checkCanStartApp()) {
            showStartAppPermissionDialog()
            return
        }
        
        // 4. 检查悬浮窗权限
        if (!Settings.canDrawOverlays(requireContext())) {
            showOverlayPermissionDialog()
            return
        }
        
        // 5. 检查录制所需权限
        if (!skipPermissionRequest) {
            val permissions = getRequiredPermissions()
            if (permissions.isNotEmpty()) {
                permissionLauncher.launch(permissions.toTypedArray())
                return
            }
        }
        
        // 所有权限都已获取，开始录制
        startRecording()
    }
    
    /**
     * 获取录制功能所需的权限列表
     */
    private fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()
        
        // 存储权限（Android 10以下需要）
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        
        // 通知权限（Android 13+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        return permissions
    }
    
    /**
     * 显示权限说明对话框
     */
    private fun showPermissionExplanationDialog(title: String, message: String, action: () -> Unit) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("前往设置") { _, _ ->
                action()
            }
            .setNegativeButton("稍后设置") { _, _ ->
                Toast.makeText(requireContext(), "需要相关权限才能正常使用录制功能", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }
    
    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkCanStartApp(): Boolean {
        val selectedAppPackageName = preferenceManager.getSelectedApp(requireContext())
        if (selectedAppPackageName.isEmpty()) {
            Toast.makeText(requireContext(), "请先选择打卡应用", Toast.LENGTH_SHORT).show()
            return false
        }
        
        try {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(selectedAppPackageName)
            return intent != null
        } catch (e: Exception) {
            return false
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.M)
    private fun showStartAppPermissionDialog() {
        showPermissionExplanationDialog(
            title = "需要启动应用权限",
            message = "录制功能需要启动目标应用进行自动化操作。\n\n请在设置中允许应用启动其他应用。",
            action = {
                try {
                    // 尝试打开应用设置页面
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: Exception) {
                    // 如果无法打开应用设置，则打开通用设置
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    startActivity(intent)
                }
            }
        )
    }
    
    @RequiresApi(Build.VERSION_CODES.M)
    private fun showOverlayPermissionDialog() {
        showPermissionExplanationDialog(
            title = "需要悬浮窗权限",
            message = "录制功能需要悬浮窗权限来显示录制控制按钮。\n\n请在设置中允许应用显示在其他应用上层。",
            action = {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivity(intent)
            }
        )
    }
    
    private fun showAccessibilityServiceDialog() {
        showPermissionExplanationDialog(
            title = "需要开启无障碍服务",
            message = "录制功能需要无障碍服务支持来记录用户操作。\n\n请在设置中开启无障碍服务。",
            action = {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
        )
    }
    
    @RequiresApi(Build.VERSION_CODES.M)
    private fun startRecording() {
        if (isRecording) return
        
        // 获取选中的应用包名
        selectedAppPackageName = preferenceManager.getSelectedApp(requireContext())
        if (selectedAppPackageName.isEmpty()) {
            Toast.makeText(requireContext(), "请先选择打卡应用", Toast.LENGTH_SHORT).show()
            return
        }
        
        isRecording = true
        recordingStartTime = System.currentTimeMillis()
        recordedActions.clear()
        
        // 启动AccessibilityService录制
        AutoPunchAccessibilityService.startRecording()
        
        // 更新UI状态
        binding.btnStartRecord.isEnabled = false
        binding.btnPauseRecord.isEnabled = true
        binding.btnStopRecord.isEnabled = true
        binding.btnSaveRecord.isEnabled = false
        
        binding.tvRecordStatus.text = getString(R.string.status_recording)
        binding.tvRecordStatus.setBackgroundResource(R.drawable.status_pending_background)
        
        // 启动目标应用
        try {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(selectedAppPackageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                
                // 延迟显示悬浮按钮，等待应用启动
                binding.root.postDelayed({
                    showFloatingButton()
                }, 2000)
                
                Toast.makeText(requireContext(), "已启动目标应用，开始录制", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "无法启动目标应用，请检查应用是否已安装", Toast.LENGTH_SHORT).show()
                stopRecording()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "启动应用失败: ${e.message}", Toast.LENGTH_SHORT).show()
            stopRecording()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("InflateParams")
    private fun showFloatingButton() {
        if (!isRecording) return
        
        // 检查悬浮窗权限
        if (!Settings.canDrawOverlays(requireContext())) {
            Toast.makeText(requireContext(), "需要悬浮窗权限才能显示录制按钮", Toast.LENGTH_LONG).show()
            showOverlayPermissionDialog()
            return
        }
        
        try {
            // 创建悬浮按钮视图
            val floatingView = LayoutInflater.from(requireContext()).inflate(R.layout.floating_record_button, null)
            
            // 设置悬浮按钮点击事件
            floatingView.setOnClickListener {
                // 停止录制并返回录制页面
                stopRecording()
                parentFragmentManager.popBackStack()
            }
            
            // 设置悬浮按钮参数
            val params = WindowManager.LayoutParams().apply {
                width = WindowManager.LayoutParams.WRAP_CONTENT
                height = WindowManager.LayoutParams.WRAP_CONTENT
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                }
                flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                format = PixelFormat.TRANSLUCENT
                gravity = Gravity.TOP or Gravity.END
                x = 50
                y = 200
            }
            
            overlayView = floatingView
            windowManager.addView(floatingView, params)
            
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "显示悬浮按钮失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun hideFloatingButton() {
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
                overlayView = null
            } catch (e: Exception) {
                // 忽略移除失败的错误
            }
        }
    }
    
    private fun pauseRecording() {
        if (!isRecording) return
        
        isRecording = false
        
        // 更新UI状态
        binding.btnStartRecord.isEnabled = true
        binding.btnPauseRecord.isEnabled = false
        binding.btnStopRecord.isEnabled = true
        binding.btnSaveRecord.isEnabled = false
        
        binding.tvRecordStatus.text = getString(R.string.status_paused)
        binding.tvRecordStatus.setBackgroundResource(R.drawable.status_pending_background)
        
        Toast.makeText(requireContext(), "录制已暂停", Toast.LENGTH_SHORT).show()
    }
    
    private fun stopRecording() {
        if (!isRecording && recordedActions.isEmpty()) return
        
        isRecording = false
        hideFloatingButton()
        
        // 停止AccessibilityService录制并获取录制的操作
        val actions = AutoPunchAccessibilityService.stopRecording()
        recordedActions.addAll(actions)
        
        // 更新UI状态
        binding.btnStartRecord.isEnabled = true
        binding.btnPauseRecord.isEnabled = false
        binding.btnStopRecord.isEnabled = false
        binding.btnSaveRecord.isEnabled = recordedActions.isNotEmpty()
        
        binding.tvRecordStatus.text = if (recordedActions.isNotEmpty()) {
            getString(R.string.status_recorded)
        } else {
            getString(R.string.status_not_recorded)
        }
        
        binding.tvRecordStatus.setBackgroundResource(
            if (recordedActions.isNotEmpty()) R.drawable.status_completed_background
            else R.drawable.status_error_background
        )
        
        Toast.makeText(requireContext(), "录制已停止，共录制 ${recordedActions.size} 个操作", Toast.LENGTH_SHORT).show()
    }
    
    private fun cancelRecording() {
        isRecording = false
        recordedActions.clear()
        hideFloatingButton()
        
        // 停止AccessibilityService录制
        AutoPunchAccessibilityService.stopRecording()
        
        // 重置UI状态
        binding.btnStartRecord.isEnabled = true
        binding.btnPauseRecord.isEnabled = false
        binding.btnStopRecord.isEnabled = false
        binding.btnSaveRecord.isEnabled = false
        
        binding.tvRecordStatus.text = getString(R.string.status_not_recorded)
        binding.tvRecordStatus.setBackgroundResource(R.drawable.status_error_background)
        
        // 返回上一页
        parentFragmentManager.popBackStack()
    }
    
    private fun saveRecording() {
        if (recordedActions.isEmpty()) {
            Toast.makeText(requireContext(), "没有录制的动作", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 保存到偏好设置
        preferenceManager.saveActionRecordStatus(true)
        
        Toast.makeText(requireContext(), "动作录制已保存", Toast.LENGTH_SHORT).show()
        
        // 返回上一页
        parentFragmentManager.popBackStack()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        hideFloatingButton()
        _binding = null
    }
    
    override fun onResume() {
        super.onResume()
        // 当从设置页面返回时，重新检查权限
        if (isRecording && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions()
        } else if (!isRecording) {
            // 如果不在录制状态，检查是否所有权限都已获取
            // 如果权限都已获取，更新UI状态
            updatePermissionStatus()
        }
    }
    
    /**
     * 更新权限状态显示
     */
    private fun updatePermissionStatus() {
        if (checkAllPermissions()) {
            binding.tvRecordStatus.text = getString(R.string.status_ready)
            binding.tvRecordStatus.setBackgroundResource(R.drawable.status_pending_background)
            binding.btnStartRecord.isEnabled = true
        } else {
            binding.tvRecordStatus.text = getString(R.string.status_not_recorded)
            binding.tvRecordStatus.setBackgroundResource(R.drawable.status_error_background)
            binding.btnStartRecord.isEnabled = true // 仍然允许点击，会触发权限检查
        }
    }
    
    /**
     * 显示权限被拒绝的对话框
     */
    private fun showPermissionDeniedDialog(deniedPermissions: List<String>) {
        val permissionNames = deniedPermissions.map { permission ->
            when (permission) {
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> "存储权限"
                Manifest.permission.READ_EXTERNAL_STORAGE -> "读取权限"
                Manifest.permission.POST_NOTIFICATIONS -> "通知权限"
                else -> "未知权限"
            }
        }.joinToString("、")
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("权限被拒绝")
            .setMessage("以下权限被拒绝：$permissionNames\n\n这些权限对于录制功能是必需的。您可以在设置中手动开启这些权限。")
            .setPositiveButton("前往设置") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", requireContext().packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_SETTINGS)
                    startActivity(intent)
                }
            }
            .setNegativeButton("稍后重试") { _, _ ->
                Toast.makeText(requireContext(), "您可以稍后重新尝试录制", Toast.LENGTH_LONG).show()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * 检查所有必要权限是否已获取
     */
    private fun checkAllPermissions(): Boolean {
        // 检查无障碍服务
        if (!AccessibilityUtil.isAccessibilityServiceEnabled(requireContext())) {
            return false
        }
        
        // 检查应用选择
        if (preferenceManager.getSelectedApp(requireContext()).isEmpty()) {
            return false
        }
        
        // 检查启动应用权限
        if (!checkCanStartApp()) {
            return false
        }
        
        // 检查悬浮窗权限
        if (!Settings.canDrawOverlays(requireContext())) {
            return false
        }
        
        // 检查其他权限
        val requiredPermissions = getRequiredPermissions()
        return requiredPermissions.isEmpty()
    }
} 