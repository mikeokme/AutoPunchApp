package com.example.autopunchapp.ui.appselection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.autopunchapp.databinding.ItemAppBinding
import com.example.autopunchapp.model.AppInfo

class AppAdapter(
    private var apps: List<AppInfo>,
    private val onAppSelected: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
    
    class AppViewHolder(val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root)
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = ItemAppBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AppViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        
        holder.binding.apply {
            ivAppIcon.setImageDrawable(app.appIcon)
            tvAppName.text = app.appName
            cbAppSelected.isChecked = app.isSelected
            
            // 设置点击事件
            root.setOnClickListener {
                val newSelected = !app.isSelected
                app.isSelected = newSelected
                cbAppSelected.isChecked = newSelected
                onAppSelected(app, newSelected)
            }
            
            cbAppSelected.setOnCheckedChangeListener { _, isChecked ->
                app.isSelected = isChecked
                onAppSelected(app, isChecked)
            }
        }
    }
    
    override fun getItemCount(): Int = apps.size
    
    fun updateApps(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }
} 