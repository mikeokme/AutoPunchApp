package com.example.autopunchapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.autopunchapp.R
import com.example.autopunchapp.databinding.FragmentHomeBinding
import com.example.autopunchapp.model.PunchLog
import com.example.autopunchapp.ui.appselection.AppSelectionFragment
import com.example.autopunchapp.ui.recordaction.RecordActionFragment
import com.example.autopunchapp.ui.timesetting.TimeSettingFragment
import com.example.autopunchapp.utils.AccessibilityUtil
import com.example.autopunchapp.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var punchLogAdapter: PunchLogAdapter
    private lateinit var preferenceManager: PreferenceManager

    companion object {
        private const val TAG = "HomeFragment"
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
            val apps = arrayOf("请选择打卡软件", "钉钉", "企业微信", "飞书", "移动办公M3")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, apps)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerAppSelection.adapter = adapter

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

                    val selectedApp = binding.spinnerAppSelection.selectedItemPosition
                    if (selectedApp == 0) {
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
                    
                    // 更新UI状态
                    binding.tvPunchStatus.text = getString(R.string.status_completed)
                    binding.tvPunchStatus.setBackgroundResource(R.drawable.status_completed_background)
                    binding.tvPunchTime.text = "上次打卡时间: ${timeFormat.format(currentTime)}"
                    preferenceManager.saveLastPunchTime(timeFormat.format(currentTime))
                    preferenceManager.savePunchStatus(true)
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

            // 打卡软件选择
            binding.spinnerAppSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    try {
                        if (position > 0) {
                            preferenceManager.saveSelectedApp(position)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "spinner onItemSelected: Error", e)
                    }
                }
                
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // 不做任何操作
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
            val selectedApp = preferenceManager.getSelectedApp()
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

            if (selectedApp > 0) {
                binding.spinnerAppSelection.setSelection(selectedApp)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}