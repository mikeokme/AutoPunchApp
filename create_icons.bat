@echo off
echo 正在创建应用图标...

echo.
echo 创建临时图标文件...

REM 创建48x48图标 (mdpi)
echo 生成 mdpi 图标...
copy nul "app\src\main\res\mipmap-mdpi\ic_launcher.png" >nul
copy nul "app\src\main\res\mipmap-mdpi\ic_launcher_round.png" >nul

REM 创建72x72图标 (hdpi)
echo 生成 hdpi 图标...
copy nul "app\src\main\res\mipmap-hdpi\ic_launcher.png" >nul
copy nul "app\src\main\res\mipmap-hdpi\ic_launcher_round.png" >nul

REM 创建96x96图标 (xhdpi)
echo 生成 xhdpi 图标...
copy nul "app\src\main\res\mipmap-xhdpi\ic_launcher.png" >nul
copy nul "app\src\main\res\mipmap-xhdpi\ic_launcher_round.png" >nul

REM 创建144x144图标 (xxhdpi)
echo 生成 xxhdpi 图标...
copy nul "app\src\main\res\mipmap-xxhdpi\ic_launcher.png" >nul
copy nul "app\src\main\res\mipmap-xxhdpi\ic_launcher_round.png" >nul

REM 创建192x192图标 (xxxhdpi)
echo 生成 xxxhdpi 图标...
copy nul "app\src\main\res\mipmap-xxxhdpi\ic_launcher.png" >nul
copy nul "app\src\main\res\mipmap-xxxhdpi\ic_launcher_round.png" >nul

echo.
echo 图标文件创建完成！
echo 注意：这些是空文件，需要替换为实际的PNG图标
echo.
echo 请访问以下网站下载时钟图标：
echo https://icons8.com/icons/set/punch-clock
echo https://pngimg.com/images/search/clock/
echo.
echo 下载后将图标重命名为 ic_launcher.png 和 ic_launcher_round.png
echo 并分别放入对应的 mipmap-* 目录中
pause 