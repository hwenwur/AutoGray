# 自动灰度 AutoGray

当打开特定应用时，自动把屏幕调成灰度模式（i.e. 开发人员选项->模拟颜色空间->全色盲模式）。

可以用于保持专注，防止无用和有害的内容吸引你的注意力。

典型用法为：将小红书、哔哩哔哩、抖音等加入灰度名单，从此不用担心被自媒体处心积虑设计的封面和标题干扰。

# 使用方法
本程序适用于安卓系统，基于无障碍和adb权限实现。

首次运行需要连接adb（有线或者无线），运行`adb shell pm grant com.xuxing.autogray android.permission.WRITE_SECURE_SETTINGS`授予权限，因为修改屏幕颜色需要特殊的权限。

根据app内提示，打开无障碍授权，并**关闭省电策略**。

注意：修改灰度列表后，需要**重启应用**才能生效（最近任务划掉、应用管理界面点击结束运行、重启手机）。

当前仅在澎湃OS系统测试过，其他系统可能存在兼容性问题，例如加减音量、下拉通知栏、唤醒语音助手时，可能会有误识别。

# 常见app的包名
```
小红书 com.xingin.xhs
哔哩哔哩 tv.danmaku.bili
豆瓣 com.douban.frodo
淘宝 com.taobao.taobao
小米音乐 com.miui.player
QQ音乐 com.tencent.qqmusic
```

# 感谢
- https://github.com/dtkav/grayscale
- Claude Opus 4
