package com.example.usageinsight.util

import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.usageinsight.service.UsageNotificationListenerService

object PermissionManager {
    private const val TAG = "PermissionManager"

    enum class PermissionType {
        USAGE_STATS,
        NOTIFICATION_LISTENER,
        POST_NOTIFICATIONS
    }

    fun hasPermission(context: Context, type: PermissionType): Boolean {
        val result = when (type) {
            PermissionType.USAGE_STATS -> hasUsageStatsPermission(context)
            PermissionType.NOTIFICATION_LISTENER -> hasNotificationListenerPermission(context)
            PermissionType.POST_NOTIFICATIONS -> hasPostNotificationsPermission(context)
        }
        Log.d(TAG, "Permission check for $type: $result")
        return result
    }

    fun requestPermission(context: Context, type: PermissionType) {
        when (type) {
            PermissionType.USAGE_STATS -> context.startActivity(getUsageStatsSettingsIntent())
            PermissionType.NOTIFICATION_LISTENER -> context.startActivity(getNotificationListenerSettingsIntent())
            PermissionType.POST_NOTIFICATIONS -> requestPostNotificationsPermission(context)
        }
    }

    private fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
        }
        
        val hasPermission = mode == AppOpsManager.MODE_ALLOWED
        Log.d(TAG, "Usage Stats Permission check result: $hasPermission (mode: $mode)")
        return hasPermission
    }

    private fun hasNotificationListenerPermission(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        val componentName = ComponentName(
            context.packageName,
            "${context.packageName}.service.UsageNotificationListenerService"
        )
        
        val componentString = componentName.flattenToString()
        val hasPermission = enabledListeners?.split(":")
            ?.any { it.trim() == componentString } == true
        
        Log.d(TAG, """
            Notification Listener Permission check:
            Package name: ${context.packageName}
            Service class: ${componentName.className}
            Component string: $componentString
            Enabled listeners: $enabledListeners
            Has permission: $hasPermission
        """.trimIndent())
        
        return hasPermission
    }

    private fun hasPostNotificationsPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Android 12 及以下不需要 POST_NOTIFICATIONS 权限
            Log.d(TAG, "Post Notifications Permission: true (not required for Android < 13)")
            return true
        }
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "Post Notifications Permission: $hasPermission")
        return hasPermission
    }

    private fun getUsageStatsSettingsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }

    private fun getNotificationListenerSettingsIntent(): Intent {
        return Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
    }

    private fun requestPostNotificationsPermission(context: Context) {
        if (context is ComponentActivity) {
            Log.d(TAG, "Requesting POST_NOTIFICATIONS permission...")
            ActivityCompat.requestPermissions(
                context,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        } else {
            Log.e(TAG, "Context is not an instance of ComponentActivity. Cannot request POST_NOTIFICATIONS.")
        }
    }
}