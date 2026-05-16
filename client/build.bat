@echo off
chcp 65001 >nul
echo ========================================
echo 音乐播放器 Tauri 打包脚本
echo ========================================
echo.

cd /d "%~dp0"

echo [1/3] 检查 Rust 工具链...
rustup show | findstr "stable-x86_64-pc-windows-msvc" >nul
if errorlevel 1 (
    echo 切换到 MSVC 工具链...
    rustup default stable-x86_64-pc-windows-msvc
)

echo.
echo [2/3] 清理前端构建产物...
if exist "..\dist" rmdir /s /q "..\dist"

echo.
echo [3/3] 执行 Tauri 构建...
echo.
npx tauri build

if errorlevel 1 (
    echo.
    echo [ERROR] 构建失败，请检查上方错误信息
    pause
    exit /b 1
)

echo.
echo ========================================
echo 构建完成！
echo.
echo 输出目录: src-tauri\target\release\
echo.
dir "src-tauri\target\release\*.exe" 2>nul
echo ========================================
pause