package com.example.usageinsight.model

data class DailyUsageSummary(
    val totalScreenTime: Long,
    val unlockCount: Int,
    val notificationCount: Int,
    val topApps: List<AppUsageSummary>,
    val nightUsagePercentage: Float  // 21:00-6:00的使用占比
) {
    val formattedTotalScreenTime: String
        get() {
            val hours = totalScreenTime / (1000 * 60 * 60)
            val minutes = (totalScreenTime / (1000 * 60)) % 60
            return "${hours}时${minutes}分钟"
        }
}

data class AppUsageSummary(
    val packageName: String,
    val appName: String,
    val usageTimeInMs: Long
)
