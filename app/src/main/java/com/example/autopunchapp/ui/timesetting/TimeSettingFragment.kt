package com.example.autopunchapp.ui.timesetting

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.autopunchapp.R
import com.example.autopunchapp.databinding.FragmentTimeSettingBinding
import com.example.autopunchapp.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

class TimeSettingFragment : Fragment() {
    
    private var _binding: FragmentTimeSettingBinding? = null
    private val binding get() = _binding!!
    
    // 上班时间段
    private var workStartHour = 9
    private var workStartMinute = 0
    private var workEndHour = 9
    private var workEndMinute = 30
    
    // 下班时间段
    private var offStartHour = 18
    private var offStartMinute = 0
    private var offEndHour = 18
    private var offEndMinute = 30
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSettingBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        loadCurrentSettings()
        setupListeners()
    }
    
    private fun loadCurrentSettings() {
        val preferenceManager = PreferenceManager(requireContext())
        
        // 加载上班时间段设置
        val workStartTime = preferenceManager.getWorkStartTime()
        val workEndTime = preferenceManager.getWorkEndTime()
        val workMinInterval = preferenceManager.getWorkMinInterval()
        val workMaxInterval = preferenceManager.getWorkMaxInterval()
        val workRandomEnabled = preferenceManager.getWorkRandomEnabled()
        
        // 解析上班时间
        val workStartParts = workStartTime.split(":")
        workStartHour = workStartParts[0].toInt()
        workStartMinute = workStartParts[1].toInt()
        
        val workEndParts = workEndTime.split(":")
        workEndHour = workEndParts[0].toInt()
        workEndMinute = workEndParts[1].toInt()
        
        // 加载下班时间段设置
        val offStartTime = preferenceManager.getOffStartTime()
        val offEndTime = preferenceManager.getOffEndTime()
        val offMinInterval = preferenceManager.getOffMinInterval()
        val offMaxInterval = preferenceManager.getOffMaxInterval()
        val offRandomEnabled = preferenceManager.getOffRandomEnabled()
        
        // 解析下班时间
        val offStartParts = offStartTime.split(":")
        offStartHour = offStartParts[0].toInt()
        offStartMinute = offStartParts[1].toInt()
        
        val offEndParts = offEndTime.split(":")
        offEndHour = offEndParts[0].toInt()
        offEndMinute = offEndParts[1].toInt()
        
        // 更新UI
        updateTimeDisplay()
        binding.etWorkMinInterval.setText(workMinInterval.toString())
        binding.etWorkMaxInterval.setText(workMaxInterval.toString())
        binding.switchWorkRandomPunch.isChecked = workRandomEnabled
        
        binding.etOffMinInterval.setText(offMinInterval.toString())
        binding.etOffMaxInterval.setText(offMaxInterval.toString())
        binding.switchOffRandomPunch.isChecked = offRandomEnabled
    }
    
    private fun setupListeners() {
        // 上班开始时间选择
        binding.timePickerWorkStart.setOnTimeChangedListener { _, hourOfDay, minute ->
            workStartHour = hourOfDay
            workStartMinute = minute
            updateTimeDisplay()
        }
        
        // 上班结束时间选择
        binding.timePickerWorkEnd.setOnTimeChangedListener { _, hourOfDay, minute ->
            workEndHour = hourOfDay
            workEndMinute = minute
            updateTimeDisplay()
        }
        
        // 下班开始时间选择
        binding.timePickerOffStart.setOnTimeChangedListener { _, hourOfDay, minute ->
            offStartHour = hourOfDay
            offStartMinute = minute
            updateTimeDisplay()
        }
        
        // 下班结束时间选择
        binding.timePickerOffEnd.setOnTimeChangedListener { _, hourOfDay, minute ->
            offEndHour = hourOfDay
            offEndMinute = minute
            updateTimeDisplay()
        }
        
        // 确认设置按钮
        binding.btnConfirmSetting.setOnClickListener {
            saveSettings()
        }
        
        // 取消设置按钮
        binding.btnCancelSetting.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }
    
    private fun updateTimeDisplay() {
        // 时间已经在TimePicker中显示，这里不需要额外处理
    }
    
    private fun saveSettings() {
        val preferenceManager = PreferenceManager(requireContext())
        
        // 验证上班时间设置
        val workStartTimeInMinutes = workStartHour * 60 + workStartMinute
        val workEndTimeInMinutes = workEndHour * 60 + workEndMinute
        
        if (workStartTimeInMinutes >= workEndTimeInMinutes) {
            Toast.makeText(requireContext(), "上班开始时间不能晚于或等于上班结束时间", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 验证下班时间设置
        val offStartTimeInMinutes = offStartHour * 60 + offStartMinute
        val offEndTimeInMinutes = offEndHour * 60 + offEndMinute
        
        if (offStartTimeInMinutes >= offEndTimeInMinutes) {
            Toast.makeText(requireContext(), "下班开始时间不能晚于或等于下班结束时间", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 获取随机间隔设置
        val workMinInterval = binding.etWorkMinInterval.text.toString().toIntOrNull() ?: 5
        val workMaxInterval = binding.etWorkMaxInterval.text.toString().toIntOrNull() ?: 15
        
        if (workMinInterval > workMaxInterval) {
            Toast.makeText(requireContext(), "上班最小间隔不能大于最大间隔", Toast.LENGTH_SHORT).show()
            return
        }
        
        val offMinInterval = binding.etOffMinInterval.text.toString().toIntOrNull() ?: 5
        val offMaxInterval = binding.etOffMaxInterval.text.toString().toIntOrNull() ?: 15
        
        if (offMinInterval > offMaxInterval) {
            Toast.makeText(requireContext(), "下班最小间隔不能大于最大间隔", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 保存上班时间段设置
        val workStartTime = String.format("%02d:%02d", workStartHour, workStartMinute)
        val workEndTime = String.format("%02d:%02d", workEndHour, workEndMinute)
        val workRandomEnabled = binding.switchWorkRandomPunch.isChecked
        
        preferenceManager.saveWorkTimeSettings(workStartTime, workEndTime, workMinInterval, workMaxInterval, workRandomEnabled)
        
        // 保存下班时间段设置
        val offStartTime = String.format("%02d:%02d", offStartHour, offStartMinute)
        val offEndTime = String.format("%02d:%02d", offEndHour, offEndMinute)
        val offRandomEnabled = binding.switchOffRandomPunch.isChecked
        
        preferenceManager.saveOffTimeSettings(offStartTime, offEndTime, offMinInterval, offMaxInterval, offRandomEnabled)
        
        Toast.makeText(requireContext(), "时间段设置已保存", Toast.LENGTH_SHORT).show()
        
        // 返回上一页
        parentFragmentManager.popBackStack()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 