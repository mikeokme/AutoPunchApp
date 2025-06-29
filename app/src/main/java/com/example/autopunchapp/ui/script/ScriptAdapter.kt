package com.example.autopunchapp.ui.script

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.autopunchapp.R
import com.example.autopunchapp.model.Script
import java.text.SimpleDateFormat
import java.util.*

class ScriptAdapter(
    private val onScriptClick: (Script) -> Unit,
    private val onScriptRun: (Script) -> Unit,
    private val onScriptEdit: (Script) -> Unit,
    private val onScriptDelete: (Script) -> Unit,
    private val onScriptShare: (Script) -> Unit
) : RecyclerView.Adapter<ScriptAdapter.ScriptViewHolder>() {
    
    private var scripts = listOf<Script>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    fun updateScripts(newScripts: List<Script>) {
        scripts = newScripts
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScriptViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_script, parent, false)
        return ScriptViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ScriptViewHolder, position: Int) {
        holder.bind(scripts[position])
    }
    
    override fun getItemCount(): Int = scripts.size
    
    inner class ScriptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvScriptName: TextView = itemView.findViewById(R.id.tv_script_name)
        private val tvScriptDescription: TextView = itemView.findViewById(R.id.tv_script_description)
        private val tvScriptAuthor: TextView = itemView.findViewById(R.id.tv_script_author)
        private val tvScriptVersion: TextView = itemView.findViewById(R.id.tv_script_version)
        private val tvScriptCategory: TextView = itemView.findViewById(R.id.tv_script_category)
        private val tvScriptUpdateTime: TextView = itemView.findViewById(R.id.tv_script_update_time)
        private val btnRun: ImageButton = itemView.findViewById(R.id.btn_run_script)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_script)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_script)
        private val btnShare: ImageButton = itemView.findViewById(R.id.btn_share_script)
        
        fun bind(script: Script) {
            tvScriptName.text = script.name
            tvScriptDescription.text = script.description.ifEmpty { "暂无描述" }
            tvScriptAuthor.text = "作者: ${script.author.ifEmpty { "未知" }}"
            tvScriptVersion.text = "版本: ${script.version}"
            tvScriptCategory.text = getCategoryText(script.category)
            tvScriptUpdateTime.text = "更新: ${dateFormat.format(Date(script.updateTime))}"
            
            // 设置点击事件
            itemView.setOnClickListener { onScriptClick(script) }
            btnRun.setOnClickListener { onScriptRun(script) }
            btnEdit.setOnClickListener { onScriptEdit(script) }
            btnDelete.setOnClickListener { onScriptDelete(script) }
            btnShare.setOnClickListener { onScriptShare(script) }
            
            // 设置分类颜色
            setCategoryColor(script.category)
        }
        
        private fun getCategoryText(category: com.example.autopunchapp.model.ScriptCategory): String {
            return when (category) {
                com.example.autopunchapp.model.ScriptCategory.PUNCH_IN -> "打卡"
                com.example.autopunchapp.model.ScriptCategory.GAME -> "游戏"
                com.example.autopunchapp.model.ScriptCategory.SOCIAL -> "社交"
                com.example.autopunchapp.model.ScriptCategory.SHOPPING -> "购物"
                com.example.autopunchapp.model.ScriptCategory.TOOL -> "工具"
                com.example.autopunchapp.model.ScriptCategory.EDUCATION -> "教育"
                com.example.autopunchapp.model.ScriptCategory.FINANCE -> "金融"
                com.example.autopunchapp.model.ScriptCategory.OTHER -> "其他"
            }
        }
        
        private fun setCategoryColor(category: com.example.autopunchapp.model.ScriptCategory) {
            val colorRes = when (category) {
                com.example.autopunchapp.model.ScriptCategory.PUNCH_IN -> R.color.primary
                com.example.autopunchapp.model.ScriptCategory.GAME -> R.color.accent
                com.example.autopunchapp.model.ScriptCategory.SOCIAL -> R.color.info
                com.example.autopunchapp.model.ScriptCategory.SHOPPING -> R.color.warning
                com.example.autopunchapp.model.ScriptCategory.TOOL -> R.color.success
                com.example.autopunchapp.model.ScriptCategory.EDUCATION -> R.color.info
                com.example.autopunchapp.model.ScriptCategory.FINANCE -> R.color.warning
                com.example.autopunchapp.model.ScriptCategory.OTHER -> R.color.gray
            }
            tvScriptCategory.setTextColor(itemView.context.getColor(colorRes))
        }
    }
} 