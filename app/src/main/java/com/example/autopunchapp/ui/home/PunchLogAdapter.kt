package com.example.autopunchapp.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.autopunchapp.R
import com.example.autopunchapp.model.PunchLog

class PunchLogAdapter(private val logs: MutableList<PunchLog>) : 
    RecyclerView.Adapter<PunchLogAdapter.LogViewHolder>() {

    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tv_log_date)
        val tvTime: TextView = view.findViewById(R.id.tv_log_time)
        val tvStatus: TextView = view.findViewById(R.id.tv_log_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_punch_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logs[position]
        holder.tvDate.text = log.date
        holder.tvTime.text = log.time
        
        if (log.isSuccessful) {
            holder.tvStatus.text = "成功"
            holder.tvStatus.setBackgroundResource(R.drawable.status_completed_background)
        } else {
            holder.tvStatus.text = "失败"
            holder.tvStatus.setBackgroundResource(R.drawable.status_error_background)
        }
    }

    override fun getItemCount() = logs.size

    fun addLog(log: PunchLog) {
        logs.add(0, log) // 添加到列表开头
        notifyItemInserted(0)
    }

    fun updateLogs(newLogs: List<PunchLog>) {
        logs.clear()
        logs.addAll(newLogs)
        notifyDataSetChanged()
    }
}