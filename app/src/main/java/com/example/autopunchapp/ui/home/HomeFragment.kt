package com.example.autopunchapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.autopunchapp.R
import com.example.autopunchapp.databinding.FragmentHomeBinding
import com.example.autopunchapp.utils.PreferenceManager

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
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
            Log.d(TAG, "onViewCreated: HomeFragment setup completed")
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated: Error setting up HomeFragment", e)
            Toast.makeText(requireContext(), "页面初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupUI() {
        try {
            // 设置基本UI状态
            binding.tvPunchStatus.text = getString(R.string.status_pending)
            binding.tvPunchStatus.setBackgroundResource(R.drawable.status_pending_background)
            
            binding.tvRecordStatus.text = getString(R.string.status_not_recorded)
            binding.tvRecordStatus.setBackgroundResource(R.drawable.status_pending_background)
            
            binding.tvPunchTime.text = "上次打卡时间: 未打卡"
            
            // 隐藏复杂的组件
            binding.spinnerAppSelection.visibility = View.GONE
            binding.rvPunchLog.visibility = View.GONE
            
        } catch (e: Exception) {
            Log.e(TAG, "setupUI: Error setting up UI", e)
            Toast.makeText(requireContext(), "界面设置失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        try {
            // 开始打卡按钮
            binding.btnStartPunch.setOnClickListener {
                Toast.makeText(requireContext(), "打卡功能开发中", Toast.LENGTH_SHORT).show()
            }

            // 设置时间段按钮
            binding.btnSetTime.setOnClickListener {
                Toast.makeText(requireContext(), "时间设置功能开发中", Toast.LENGTH_SHORT).show()
            }

            // 录制动作按钮
            binding.btnRecordAction.setOnClickListener {
                Toast.makeText(requireContext(), "动作录制功能开发中", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "setupListeners: Error setting up listeners", e)
            Toast.makeText(requireContext(), "事件设置失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}