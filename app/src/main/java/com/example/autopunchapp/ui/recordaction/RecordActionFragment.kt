package com.example.autopunchapp.ui.recordaction

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.PixelFormat
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
        
        setupUI()
        setupListeners()
    }
    
    private fun setupUI() {
        // 检查无障碍服务状态
        if (!AccessibilityUtil.isAccessibilityServiceEnabled(requireContext())) {
            binding.tvRecordStatus.text = getString(R.string.status_not_recorded)
            binding.tvRecordStatus.setBackgroundResource(R.drawable.status_error_background)
        } else {
            binding.tvRecordStatus.text = getString(R.string.status_ready)
            binding.tvRecordStatus.setBackgroundResource(R.drawable.status_pending_background)
        }
    }
    
    private fun setupListeners() {
        // 开始录制按钮
        binding.btnStartRecord.setOnClickListener {
            if (!AccessibilityUtil.isAccessibilityServiceEnabled(requireContext())) {
                showAccessibilityServiceDialog()
                return@setOnClickListener
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                startRecording()
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
                Toast.makeText(requireContext(), "无法启动目标应用", Toast.LENGTH_SHORT).show()
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
    
    @RequiresApi(Build.VERSION_CODES.M)
    private fun showOverlayPermissionDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("需要悬浮窗权限")
            .setMessage("录制功能需要悬浮窗权限来显示录制控制按钮，请前往系统设置开启。")
            .setPositiveButton("前往设置") { _, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivity(intent)
            }
            .setNegativeButton("取消") { _, _ ->
                stopRecording()
            }
            .show()
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
    
    private fun showAccessibilityServiceDialog() {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("需要开启无障碍服务")
            .setMessage("录制功能需要无障碍服务支持，请前往系统设置开启。")
            .setPositiveButton("前往设置") { _, _ ->
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        hideFloatingButton()
        _binding = null
    }
} 