package com.example.usageinsight.util

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import android.util.Log

object PermissionUtils {
    private const val TAG = "PermissionUtils"

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        val hasPermission = mode == AppOpsManager.MODE_ALLOWED
        Log.d(TAG, "Usage Stats Permission: $hasPermission")
        return hasPermission
    }

    fun hasNotificationListenerPermission(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        val serviceComponent = ComponentName(
            context.packageName,
            "${context.packageName}.service.UsageNotificationListenerService"
        )
        val hasPermission = enabledListeners?.split(":")
            ?.any { it.trim() == serviceComponent.flattenToString() } == true

        Log.d(TAG, "Notification Listener Permission: $hasPermission")
        Log.d(TAG, "Enabled Listeners: $enabledListeners")
        Log.d(TAG, "Service Component: ${serviceComponent.flattenToString()}")

        return hasPermission
    }

    fun getPermissionSettingsIntent(type: PermissionType): Intent = when (type) {
        PermissionType.USAGE_STATS -> Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        PermissionType.NOTIFICATION_LISTENER -> Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }

    enum class PermissionType {
        USAGE_STATS,
        NOTIFICATION_LISTENER
    }
}
