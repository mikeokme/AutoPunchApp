@echo off
echo 正在测试自动打卡应用...

echo.
echo 1. 清理应用数据...
adb shell pm clear com.example.autopunchapp

echo.
echo 2. 安装应用...
adb install -r app/build/outputs/apk/debug/app-debug.apk

echo.
echo 3. 启动应用...
adb shell am start -n com.example.autopunchapp/.MainActivity

echo.
echo 4. 等待应用启动...
timeout /t 3 /nobreak > nul

echo.
echo 5. 检查应用是否正在运行...
adb shell dumpsys activity activities | findstr "autopunchapp"

echo.
echo 6. 查看应用日志...
echo 正在查看最近的日志...
adb logcat -d | findstr "MainActivity\|HomeFragment\|AndroidRuntime\|FATAL" | tail -20

echo.
echo 测试完成！
pause 