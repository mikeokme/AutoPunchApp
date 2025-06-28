@echo off
echo ========================================
echo 自动打卡应用图标安装助手
echo ========================================
echo.

echo 请按照以下步骤操作：
echo.
echo 1. 在浏览器中打开 generate_icons.html 文件
echo 2. 点击"下载所有图标"按钮下载PNG文件
echo 3. 将下载的图标文件移动到以下目录：
echo.

echo 将以下文件移动到对应目录：
echo.
echo ic_launcher_mdpi.png     -> app\src\main\res\mipmap-mdpi\ic_launcher.png
echo ic_launcher_round_mdpi.png -> app\src\main\res\mipmap-mdpi\ic_launcher_round.png
echo.
echo ic_launcher_hdpi.png     -> app\src\main\res\mipmap-hdpi\ic_launcher.png
echo ic_launcher_round_hdpi.png -> app\src\main\res\mipmap-hdpi\ic_launcher_round.png
echo.
echo ic_launcher_xhdpi.png    -> app\src\main\res\mipmap-xhdpi\ic_launcher.png
echo ic_launcher_round_xhdpi.png -> app\src\main\res\mipmap-xhdpi\ic_launcher_round.png
echo.
echo ic_launcher_xxhdpi.png   -> app\src\main\res\mipmap-xxhdpi\ic_launcher.png
echo ic_launcher_round_xxhdpi.png -> app\src\main\res\mipmap-xxhdpi\ic_launcher_round.png
echo.
echo ic_launcher_xxxhdpi.png  -> app\src\main\res\mipmap-xxxhdpi\ic_launcher.png
echo ic_launcher_round_xxxhdpi.png -> app\src\main\res\mipmap-xxxhdpi\ic_launcher_round.png
echo.

echo 4. 移动完成后，运行以下命令重新构建应用：
echo    ./gradlew assembleDebug
echo.

echo 5. 安装应用到设备：
echo    adb install -r app/build/outputs/apk/debug/app-debug.apk
echo.

echo 现在是否要打开图标生成器？
set /p choice="输入 y 打开图标生成器，或按回车键退出: "
if /i "%choice%"=="y" (
    start generate_icons.html
)

echo.
echo 图标安装说明完成！
pause 