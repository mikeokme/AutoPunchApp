package com.example.autopunchapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.autopunchapp.databinding.ActivityMainBinding
import com.example.autopunchapp.ui.appselection.AppSelectionFragment
import com.example.autopunchapp.ui.home.HomeFragment
import com.example.autopunchapp.ui.notification.NotificationFragment
import com.example.autopunchapp.ui.punchplan.PunchPlanFragment
import com.example.autopunchapp.ui.recordaction.RecordActionFragment
import com.example.autopunchapp.ui.settings.SettingsFragment
import com.example.autopunchapp.ui.timesetting.TimeSettingFragment
import com.example.autopunchapp.utils.AccessibilityUtil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        // 检查无障碍服务是否开启
        if (!AccessibilityUtil.isAccessibilityServiceEnabled(this)) {
            showAccessibilityServiceDialog()
        }

        // 默认显示主页面
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // 设置悬浮按钮点击事件
        binding.fabSettings.setOnClickListener {
            loadFragment(SettingsFragment())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_home -> {
                loadFragment(HomeFragment())
                true
            }
            R.id.action_app_selection -> {
                loadFragment(AppSelectionFragment())
                true
            }
            R.id.action_record_action -> {
                loadFragment(RecordActionFragment())
                true
            }
            R.id.action_time_setting -> {
                loadFragment(TimeSettingFragment())
                true
            }
            R.id.action_punch_plan -> {
                loadFragment(PunchPlanFragment())
                true
            }
            R.id.action_notification -> {
                loadFragment(NotificationFragment())
                true
            }
            R.id.action_settings -> {
                loadFragment(SettingsFragment())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        currentFragment = fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_content_main, fragment)
            .commit()
    }

    private fun showAccessibilityServiceDialog() {
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
    }
}