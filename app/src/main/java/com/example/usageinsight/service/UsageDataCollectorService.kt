package com.example.usageinsight.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.usageinsight.R
import com.example.usageinsight.UsageInsightApp
import com.example.usageinsight.data.dao.AppUsageDao
import com.example.usageinsight.data.entity.AppUsageEntity
import com.example.usageinsight.util.getTodayStartMillis
import com.example.usageinsight.util.PermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class UsageDataCollectorService : Service() {
    companion object {
        const val ACTION_COLLECT_NOW = "com.example.usageinsight.COLLECT_NOW"
        private const val NOTIFICATION_ID = 1
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var appUsageDao: AppUsageDao
    private lateinit var usageStatsManager: UsageStatsManager
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval = 1000L * 60 * 5 // 5分钟更新一次
    
    private val updateRunnable = object : Runnable {
        override fun run() {
            collectData()
            handler.postDelayed(this, updateInterval)
        }
    }

    private val commonAppNames = mapOf(
        "com.tencent.mm" to "微信",
        "com.zhihu.android" to "知乎",
        "com.tencent.mobileqq" to "QQ",
        "com.xunlei.downloadprovider" to "迅雷",
        "com.ss.android.ugc.aweme" to "抖音",
        "com.ss.android.article.news" to "今日头条",
        "com.alibaba.android.rimet" to "钉钉",
        "com.tencent.wework" to "企业微信",
        "com.netease.cloudmusic" to "网易云音乐",
        "com.sina.weibo" to "微博",
        "com.eg.android.AlipayGphone" to "支付宝",
        "com.taobao.taobao" to "淘宝",
        "com.jingdong.app.mall" to "京东",
        "com.baidu.searchbox" to "百度",
        "com.xunmeng.pinduoduo" to "拼多多",
        "com.smile.gifmaker" to "快手",
        "com.tencent.weread" to "微信读书",
        "com.bilibili.app.in" to "哔哩哔哩",
        "com.xiaomi.market" to "小米应用商店",
        "com.android.vending" to "Google Play",
        "com.google.android.youtube" to "YouTube",
        "com.microsoft.teams" to "Teams",
        "com.google.android.gm" to "Gmail",
        "com.microsoft.office.outlook" to "Outlook",
        "com.google.android.apps.maps" to "Google Maps",
        "com.android.chrome" to "Chrome",
        "org.mozilla.firefox" to "Firefox",
        "com.microsoft.edge" to "Edge"
    )

    private val localBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(this)
    }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_COLLECT_NOW) {
                serviceScope.launch {
                    collectUsageData()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        handler.post(updateRunnable)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            
            // 初始化 DAO 和 UsageStatsManager
            appUsageDao = (application as UsageInsightApp).database.appUsageDao()
            usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
            
            // 使用本地广播管理器注册接收器
            localBroadcastManager.registerReceiver(
                broadcastReceiver,
                IntentFilter(ACTION_COLLECT_NOW)
            )
            
            startDataCollection()
        } catch (e: Exception) {
            Log.e("UsageDataCollector", "服务启动失败", e)
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "UsageDataCollector",
                "Usage Data Collector",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "UsageDataCollector")
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
        }
        
        return builder
            .setContentTitle("使用情况统计")
            .setContentText("正在后台统计应用使用情况")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun startDataCollection() {
        serviceScope.launch {
            try {
                while (true) {
                    if (PermissionManager.hasPermission(
                            applicationContext,
                            PermissionManager.PermissionType.USAGE_STATS
                        )
                    ) {
                        collectUsageData()
                    }
                    delay(TimeUnit.MINUTES.toMillis(15))
                }
            } catch (e: Exception) {
                Log.e("UsageDataCollector", "Data collection failed", e)
            }
        }
    }

    private fun collectData() {
        val currentTime = System.currentTimeMillis()
        val todayStart = getTodayStartMillis()
        
        Log.d("UsageDataCollector", """
            开始收集数据：
            当前时间：${Instant.ofEpochMilli(currentTime)}
            今日开始：${Instant.ofEpochMilli(todayStart)}
        """.trimIndent())
        
        // 获取昨天的开始时间，用于计算今天的使用时长
        val yesterdayStart = todayStart - 24 * 60 * 60 * 1000
        
        // 获取两天的使用统计
        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            yesterdayStart,
            currentTime
        )
        
        Log.d("UsageDataCollector", "收集到 ${stats.size} 条使用记录")
        
        CoroutineScope(Dispatchers.IO).launch {
            appUsageDao.deleteDataForDate(todayStart)
            
            stats.forEach { stat ->
                // 只处理今天的数据
                if (stat.lastTimeUsed >= todayStart && stat.totalTimeInForeground > 0) {
                    // 计算今天的使用时长
                    val usageTimeToday = if (stat.firstTimeStamp < todayStart) {
                        // 如果应用在今天之前就开始使用，只计算今天的部分
                        stat.lastTimeUsed - todayStart
                    } else {
                        // 如果应用是今天开始使用的，使用总时长
                        stat.totalTimeInForeground
                    }.coerceAtMost(24 * 60 * 60 * 1000) // 确保不超过24小时
                    
                    val appUsage = AppUsageEntity(
                        packageName = stat.packageName,
                        appName = getAppName(stat.packageName),
                        usageTimeInMs = usageTimeToday,
                        lastTimeUsed = stat.lastTimeUsed,
                        date = todayStart
                    )
                    
                    Log.d("UsageDataCollector", """
                        插入数据:
                        应用：${appUsage.appName}
                        包名：${appUsage.packageName}
                        使用时长：${appUsage.usageTimeInMs / 1000 / 60}分钟
                        最后使用：${Instant.ofEpochMilli(appUsage.lastTimeUsed)}
                        首次使用：${Instant.ofEpochMilli(stat.firstTimeStamp)}
                    """.trimIndent())
                    
                    appUsageDao.insertAppUsage(appUsage)
                }
            }
        }
    }

    private suspend fun collectUsageData() {
        collectData()
    }

    private fun getAppName(packageName: String): String {
        // 首先检查常用应用映射
        commonAppNames[packageName]?.let { return it }

        // 如果不在映射表中，尝试从系统获取
        return try {
            val packageManager = applicationContext.packageManager
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0)
            ).toString()
        } catch (e: Exception) {
            // 如果获取失败，返回包名的最后一部分
            packageName.split(".").last()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        localBroadcastManager.unregisterReceiver(broadcastReceiver)
        serviceScope.cancel()
    }
}
