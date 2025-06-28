package com.example.autopunchapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
    private var fragmentStack = mutableListOf<Fragment>()

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

            // 默认显示主页面
            if (savedInstanceState == null) {
                loadFragment(HomeFragment())
            }

            // 设置悬浮按钮点击事件
            binding.fabSettings.setOnClickListener {
                loadFragment(SettingsFragment())
            }
            
            // 设置底部导航按钮
            setupBottomNavigation()
            
            Log.d(TAG, "onCreate: MainActivity initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Error initializing MainActivity", e)
            Toast.makeText(this, "应用初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupBottomNavigation() {
        // 返回上一级按钮
        binding.btnBack.setOnClickListener {
            goBack()
        }

        // 返回主页按钮
        binding.btnHome.setOnClickListener {
            goHome()
        }

        // 进入桌面按钮
        binding.btnDesktop.setOnClickListener {
            goToDesktop()
        }
    }

    private fun goBack() {
        try {
            if (fragmentStack.size > 1) {
                // 移除当前Fragment
                fragmentStack.removeAt(fragmentStack.size - 1)
                // 加载上一个Fragment
                val previousFragment = fragmentStack.last()
                loadFragment(previousFragment, false)
                Toast.makeText(this, "已返回上一级", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "已在主页", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "goBack: Error", e)
            Toast.makeText(this, "返回失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goHome() {
        try {
            // 清空Fragment栈
            fragmentStack.clear()
            // 加载主页
            loadFragment(HomeFragment())
            Toast.makeText(this, "已返回主页", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "goHome: Error", e)
            Toast.makeText(this, "返回主页失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToDesktop() {
        try {
            // 发送HOME键意图
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(homeIntent)
            Toast.makeText(this, "已进入桌面", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "goToDesktop: Error", e)
            Toast.makeText(this, "进入桌面失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        try {
            menuInflater.inflate(R.menu.menu_main, menu)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "onCreateOptionsMenu: Error inflating menu", e)
            return false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return try {
            when (item.itemId) {
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
        } catch (e: Exception) {
            Log.e(TAG, "onOptionsItemSelected: Error handling menu item", e)
            Toast.makeText(this, "操作失败: ${e.message}", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun loadFragment(fragment: Fragment, addToStack: Boolean = true) {
        try {
            Log.d(TAG, "loadFragment: Loading fragment ${fragment.javaClass.simpleName}")
            currentFragment = fragment
            
            if (addToStack) {
                fragmentStack.add(fragment)
            }
            
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_main, fragment)
                .commit()
            Log.d(TAG, "loadFragment: Fragment loaded successfully")
        } catch (e: Exception) {
            Log.e(TAG, "loadFragment: Error loading fragment", e)
            Toast.makeText(this, "页面加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
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

    override fun onBackPressed() {
        // 重写返回键，使用我们的返回逻辑
        goBack()
    }
}