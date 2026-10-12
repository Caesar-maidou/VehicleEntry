# 车辆临时入校申请 Android 工程

## 功能
- 离线运行，数据和证件照片保存在本机 WebView 存储中，不会主动上传。
- 输入/修改时自动保存；下次打开自动恢复上次内容。
- 顶部右上角三个点菜单：保存、导出备份、导入备份、清空内容。
- 底部不再显示保存按钮。
- 可通过系统文件选择器上传证件图片，系统文件选择器导入/导出 JSON 备份。

## 构建 APK
1. 电脑安装 Android Studio（建议当前稳定版）及 Android SDK Platform 35。
2. Android Studio 选择 **Open**，打开本文件夹 `VehicleEntryAndroid`。
3. 等待 Gradle 同步完成，选择 Build → Build Bundle(s) / APK(s) → Build APK(s)。
4. APK 通常生成在 `app/build/outputs/apk/debug/app-debug.apk`。

## Releases

Pre-built APKs are available on the GitHub Releases page.

## License

This project is licensed under the [MIT Licence](LICENCE).

Copyright (c) 2026 maidou
