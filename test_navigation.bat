@echo off
echo ========================================
echo 底部导航功能测试
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
echo ========================================
echo 底部导航功能说明
echo ========================================
echo.
echo ✅ 底部导航栏功能：
echo.
echo 🔙 返回上一级按钮：
echo    - 功能：返回到上一个页面
echo    - 逻辑：维护Fragment栈，移除当前页面，显示上一个页面
echo    - 提示：显示"已返回上一级"或"已在主页"
echo.
echo 🏠 返回主页按钮：
echo    - 功能：直接返回到主页面
echo    - 逻辑：清空Fragment栈，加载HomeFragment
echo    - 提示：显示"已返回主页"
echo.
echo 📱 进入桌面按钮：
echo    - 功能：退出应用到系统桌面
echo    - 逻辑：发送HOME键意图
echo    - 提示：显示"已进入桌面"
echo.
echo 📋 测试步骤：
echo 1. 检查底部是否显示三个按钮
echo 2. 点击菜单进入不同页面
echo 3. 测试返回上一级功能
echo 4. 测试返回主页功能
echo 5. 测试进入桌面功能
echo 6. 测试物理返回键功能
echo.
echo 🎯 特殊功能：
echo - 物理返回键已重写，使用自定义返回逻辑
echo - Fragment栈管理，支持多级页面导航
echo - 所有页面都有底部边距，避免被导航栏遮挡
echo.
echo 如果遇到问题，请查看日志：
echo adb logcat | findstr "MainActivity\|Navigation"
echo.
pause 