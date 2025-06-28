@echo off
echo ========================================
echo 自动打卡应用完整功能测试
echo ========================================
echo.

echo 正在安装应用...
adb install -r app/build/outputs/apk/debug/app-debug.apk

echo.
echo 正在启动应用...
adb shell am start -n com.example.autopunchapp/.MainActivity

echo.
echo 等待应用启动...
timeout /t 3 /nobreak > nul

echo.
echo 应用功能说明：
echo.
echo ✅ 主界面功能：
echo    - 当前打卡状态显示
echo    - 打卡软件选择下拉框
echo    - 动作录制状态显示
echo    - 开始打卡按钮
echo    - 设置时间段按钮
echo    - 录制动作按钮
echo    - 打卡日志列表
echo.
echo ✅ 菜单功能：
echo    - 首页
echo    - 选择打卡软件
echo    - 录制打卡动作
echo    - 设置时间段
echo    - 打卡计划
echo    - 通知与提醒
echo    - 设置
echo.
echo ✅ 悬浮按钮：
echo    - 快速进入设置页面
echo.
echo ✅ 无障碍服务：
echo    - 自动检测无障碍服务状态
echo    - 引导用户开启无障碍服务
echo.
echo 应用已启动，请检查以下功能：
echo 1. 主界面是否正常显示
echo 2. 菜单按钮是否可用
echo 3. 各个功能按钮是否响应
echo 4. 页面跳转是否正常
echo.
echo 如果遇到问题，请查看日志：
echo adb logcat | findstr "MainActivity\|HomeFragment\|AndroidRuntime"
echo.
pause 