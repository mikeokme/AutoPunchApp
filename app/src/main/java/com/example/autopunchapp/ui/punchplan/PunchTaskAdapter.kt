package com.example.autopunchapp.ui.punchplan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.autopunchapp.R
import com.example.autopunchapp.model.PunchTask

class PunchTaskAdapter(
    private val tasks: MutableList<PunchTask>,
    private val onTaskStatusChanged: (PunchTask, Boolean) -> Unit
) : RecyclerView.Adapter<PunchTaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvAppName: TextView = view.findViewById(R.id.tv_task_app_name)
        val tvTimeRange: TextView = view.findViewById(R.id.tv_task_time_range)
        val tvInterval: TextView = view.findViewById(R.id.tv_task_interval)
        val switchEnabled: Switch = view.findViewById(R.id.switch_task_enabled)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_punch_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        
        holder.tvAppName.text = task.appName
        holder.tvTimeRange.text = "${task.startTime} - ${task.endTime}"
        holder.tvInterval.text = "${task.minInterval}-${task.maxInterval}分钟"
        holder.switchEnabled.isChecked = task.isEnabled
        
        holder.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            onTaskStatusChanged(task, isChecked)
        }
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<PunchTask>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
} 