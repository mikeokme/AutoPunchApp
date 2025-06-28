@echo off
echo ========================================
echo 自动打卡应用 - 脚本版本构建
echo ========================================
echo.

echo 正在清理项目...
gradlew clean

echo.
echo 正在构建脚本版本...
gradlew assembleDebug --no-daemon

echo.
echo 正在安装到设备...
adb install -r app/build/outputs/apk/debug/app-debug.apk

echo.
echo 构建完成！
echo 新功能包括：
echo - JavaScript脚本引擎
echo - 脚本编辑器
echo - 脚本管理功能
echo - 脚本模板和API帮助
echo - 执行日志功能
echo.
pause 