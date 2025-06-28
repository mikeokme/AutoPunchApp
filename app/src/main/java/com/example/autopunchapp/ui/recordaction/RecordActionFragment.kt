package com.example.autopunchapp.ui.recordaction

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.autopunchapp.R
import com.example.autopunchapp.databinding.FragmentRecordActionBinding
import com.example.autopunchapp.model.Action
import com.example.autopunchapp.model.ActionType
import com.example.autopunchapp.model.PunchAction
import com.example.autopunchapp.service.AutoPunchAccessibilityService
import com.example.autopunchapp.utils.AccessibilityUtil
import com.example.autopunchapp.utils.PreferenceManager
import java.util.*

class RecordActionFragment : Fragment() {
    
    private var _binding: FragmentRecordActionBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    
    private val recordedActions = mutableListOf<Action>()
    private var isRecording = false
    private var recordingStartTime = 0L
    
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
            
            startRecording()
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
    
    private fun startRecording() {
        if (isRecording) return
        
        isRecording = true
        recordingStartTime = System.currentTimeMillis()
        recordedActions.clear()
        
        // 更新UI状态
        binding.btnStartRecord.isEnabled = false
        binding.btnPauseRecord.isEnabled = true
        binding.btnStopRecord.isEnabled = true
        binding.btnSaveRecord.isEnabled = false
        
        binding.tvRecordStatus.text = getString(R.string.status_recording)
        binding.tvRecordStatus.setBackgroundResource(R.drawable.status_pending_background)
        
        Toast.makeText(requireContext(), "开始录制，请操作目标应用", Toast.LENGTH_SHORT).show()
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
        
        Toast.makeText(requireContext(), "录制已停止", Toast.LENGTH_SHORT).show()
    }
    
    private fun cancelRecording() {
        isRecording = false
        recordedActions.clear()
        
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
        
        // 获取选中的应用包名
        val selectedApp = preferenceManager.getSelectedApp()
        if (selectedApp == 0) {
            Toast.makeText(requireContext(), "请先选择打卡应用", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 创建打卡动作对象
        val punchAction = PunchAction(
            appPackageName = selectedApp.toString(),
            actions = recordedActions.toList()
        )
        
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
        _binding = null
    }
} 