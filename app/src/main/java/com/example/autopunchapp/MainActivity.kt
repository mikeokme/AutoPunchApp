package com.example.autopunchapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.autopunchapp.databinding.ActivityMainBinding
import com.example.autopunchapp.utils.AccessibilityUtil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "onCreate: Starting MainActivity")
            
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            setSupportActionBar(binding.toolbar)

            // 检查无障碍服务是否开启
            if (!AccessibilityUtil.isAccessibilityServiceEnabled(this)) {
                showAccessibilityServiceDialog()
            }

            // 设置悬浮按钮点击事件
            binding.fabSettings.setOnClickListener {
                Toast.makeText(this, "设置功能开发中", Toast.LENGTH_SHORT).show()
            }
            
            // 显示欢迎信息
            Toast.makeText(this, "自动打卡应用启动成功！", Toast.LENGTH_SHORT).show()
            
            Log.d(TAG, "onCreate: MainActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Error initializing MainActivity", e)
            Toast.makeText(this, "应用初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun showAccessibilityServiceDialog() {
        try {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("需要开启无障碍服务")
                .setMessage("自动打卡功能需要无障碍服务支持，请前往系统设置开启。")
                .setPositiveButton("前往设置") { _, _ ->
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("取消") { _, _ ->
                    Toast.makeText(this, "未开启无障碍服务，部分功能将无法使用", Toast.LENGTH_LONG).show()
                }
                .setCancelable(false)
                .show()
        } catch (e: Exception) {
            Log.e(TAG, "showAccessibilityServiceDialog: Error showing dialog", e)
            Toast.makeText(this, "对话框显示失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}