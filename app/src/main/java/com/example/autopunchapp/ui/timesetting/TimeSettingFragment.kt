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
    
    private var startHour = 9
    private var startMinute = 0
    private var endHour = 18
    private var endMinute = 0
    
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
        // 加载当前设置
        val startTime = PreferenceManager(requireContext()).getStartTime()
        val endTime = PreferenceManager(requireContext()).getEndTime()
        val minInterval = PreferenceManager(requireContext()).getMinInterval()
        val maxInterval = PreferenceManager(requireContext()).getMaxInterval()
        val randomEnabled = PreferenceManager(requireContext()).getRandomEnabled()
        
        // 解析时间
        val startParts = startTime.split(":")
        startHour = startParts[0].toInt()
        startMinute = startParts[1].toInt()
        
        val endParts = endTime.split(":")
        endHour = endParts[0].toInt()
        endMinute = endParts[1].toInt()
        
        // 更新UI
        updateTimeDisplay()
        binding.etMinInterval.setText(minInterval.toString())
        binding.etMaxInterval.setText(maxInterval.toString())
        binding.switchRandomPunch.isChecked = randomEnabled
    }
    
    private fun setupListeners() {
        // 开始时间选择
        binding.timePickerStart.setOnTimeChangedListener { _, hourOfDay, minute ->
            startHour = hourOfDay
            startMinute = minute
            updateTimeDisplay()
        }
        
        // 结束时间选择
        binding.timePickerEnd.setOnTimeChangedListener { _, hourOfDay, minute ->
            endHour = hourOfDay
            endMinute = minute
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
        // 验证时间设置
        val startTimeInMinutes = startHour * 60 + startMinute
        val endTimeInMinutes = endHour * 60 + endMinute
        
        if (startTimeInMinutes >= endTimeInMinutes) {
            Toast.makeText(requireContext(), "开始时间不能晚于或等于结束时间", Toast.LENGTH_SHORT).show()
            return
        }
        
        val minInterval = binding.etMinInterval.text.toString().toIntOrNull() ?: 1
        val maxInterval = binding.etMaxInterval.text.toString().toIntOrNull() ?: 3
        
        if (minInterval > maxInterval) {
            Toast.makeText(requireContext(), "最小间隔不能大于最大间隔", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 保存设置
        val startTime = String.format("%02d:%02d", startHour, startMinute)
        val endTime = String.format("%02d:%02d", endHour, endMinute)
        val randomEnabled = binding.switchRandomPunch.isChecked
        
        PreferenceManager(requireContext()).saveTimeSettings(startTime, endTime, minInterval, maxInterval, randomEnabled)
        
        Toast.makeText(requireContext(), "时间设置已保存", Toast.LENGTH_SHORT).show()
        
        // 返回上一页
        parentFragmentManager.popBackStack()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 