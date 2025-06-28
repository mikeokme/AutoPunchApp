package com.example.autopunchapp.ui.punchplan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.autopunchapp.R
import com.example.autopunchapp.databinding.FragmentPunchPlanBinding
import com.example.autopunchapp.model.PunchTask
import com.example.autopunchapp.utils.PreferenceManager
import java.util.*

class PunchPlanFragment : Fragment() {
    
    private var _binding: FragmentPunchPlanBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var taskAdapter: PunchTaskAdapter
    private val taskList = mutableListOf<PunchTask>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPunchPlanBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        preferenceManager = PreferenceManager(requireContext())
        
        setupRecyclerView()
        loadTasks()
        setupListeners()
    }
    
    private fun setupRecyclerView() {
        taskAdapter = PunchTaskAdapter(taskList) { task, isEnabled ->
            // 处理任务启用/禁用状态变化
            task.isEnabled = isEnabled
            preferenceManager.savePunchTask(task)
        }
        
        binding.rvTaskList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = taskAdapter
        }
    }
    
    private fun loadTasks() {
        taskList.clear()
        taskList.addAll(preferenceManager.getPunchTasks())
        taskAdapter.notifyDataSetChanged()
        
        updateEmptyState()
    }
    
    private fun setupListeners() {
        // 添加任务按钮
        binding.btnAddTask.setOnClickListener {
            showAddTaskDialog()
        }
    }
    
    private fun getSelectedTask(): PunchTask? {
        return taskList.find { it.isEnabled }
    }
    
    private fun showAddTaskDialog() {
        // 这里应该显示一个对话框来添加新任务
        // 暂时创建一个示例任务
        val newTask = PunchTask(
            appPackageName = "com.example.app",
            appName = "示例应用",
            startTime = "09:00",
            endTime = "18:00",
            minInterval = 1,
            maxInterval = 3,
            isEnabled = true,
            isRandomEnabled = true
        )
        
        preferenceManager.savePunchTask(newTask)
        loadTasks()
        
        Toast.makeText(requireContext(), "任务已添加", Toast.LENGTH_SHORT).show()
    }
    
    private fun showEditTaskDialog(task: PunchTask) {
        // 这里应该显示编辑任务的对话框
        Toast.makeText(requireContext(), "编辑任务功能开发中", Toast.LENGTH_SHORT).show()
    }
    
    private fun showDeleteTaskDialog(task: PunchTask) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("删除任务")
            .setMessage("确定要删除任务 \"${task.appName}\" 吗？")
            .setPositiveButton("删除") { _, _ ->
                preferenceManager.deletePunchTask(task.id)
                loadTasks()
                Toast.makeText(requireContext(), "任务已删除", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun updateEmptyState() {
        // 暂时不处理空状态显示，因为布局中没有tvEmptyState
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 