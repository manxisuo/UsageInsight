package com.example.usageinsight

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.PackageStats
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.usageinsight.service.UsageDataCollectorService
import com.example.usageinsight.ui.navigation.MainNavigation
import com.example.usageinsight.ui.theme.UsageInsightTheme
import com.example.usageinsight.util.PermissionManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import android.app.AppOpsManager

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"
    private val REQUEST_CODE_GOOGLE_FIT = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UsageInsightTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation()
                }
            }
        }
        
        // 检查使用情况访问权限
        if (!hasUsageStatsPermission()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        
        // 启动数据收集服务
        startService(Intent(this, UsageDataCollectorService::class.java))
    }

    private fun checkAndUpdatePermissions(onPermissionsUpdated: (Set<PermissionManager.PermissionType>) -> Unit) {
        val missing = checkMissingPermissions()
        Log.d(TAG, "Checking permissions, missing: $missing")
        onPermissionsUpdated(missing)
    }

    private fun checkMissingPermissions(): Set<PermissionManager.PermissionType> {
        val missing = mutableSetOf<PermissionManager.PermissionType>()

        if (!PermissionManager.hasPermission(this, PermissionManager.PermissionType.USAGE_STATS)) {
            Log.d(TAG, "Missing Usage Stats Permission")
            missing.add(PermissionManager.PermissionType.USAGE_STATS)
        } else {
            Log.d(TAG, "Usage Stats Permission granted")
        }
        
        if (!PermissionManager.hasPermission(this, PermissionManager.PermissionType.NOTIFICATION_LISTENER)) {
            Log.d(TAG, "Missing Notification Listener Permission")
            missing.add(PermissionManager.PermissionType.NOTIFICATION_LISTENER)
        } else {
            Log.d(TAG, "Notification Listener Permission granted")
        }
        
        if (!PermissionManager.hasPermission(this, PermissionManager.PermissionType.POST_NOTIFICATIONS)) {
            Log.d(TAG, "Missing Post Notifications Permission")
            missing.add(PermissionManager.PermissionType.POST_NOTIFICATIONS)
        } else {
            Log.d(TAG, "Post Notifications Permission granted")
        }

        Log.d(TAG, "Missing permissions: $missing")
        return missing
    }

    private fun requestPermissions(permissions: Set<PermissionManager.PermissionType>) {
        Log.d(TAG, "Requesting permissions: $permissions")
        permissions.forEach { permission ->
            Log.d(TAG, "Requesting permission: $permission")
            PermissionManager.requestPermission(this, permission)
        }
    }

    private fun startUsageDataCollector() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(this, UsageDataCollectorService::class.java))
            } else {
                startService(Intent(this, UsageDataCollectorService::class.java))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start UsageDataCollectorService", e)
        }
    }

    private fun requestFitnessPermissions() {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
            .build()

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                REQUEST_CODE_GOOGLE_FIT,
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_GOOGLE_FIT -> {
                if (resultCode == Activity.RESULT_OK) {
                    // 权限已授予，可以开始访问健康数据
                } else {
                    // 用户拒绝了权限
                }
            }
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}

@Composable
fun PermissionDialog(
    missingPermissions: Set<PermissionManager.PermissionType>,
    onDismiss: () -> Unit,
    onRequestPermissions: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("需要权限") },
        text = {
            Text(buildString {
                append("此应用需要以下权限才能正常工作：\n\n")
                if (PermissionManager.PermissionType.USAGE_STATS in missingPermissions) {
                    append("• 使用情况访问权限\n")
                }
                if (PermissionManager.PermissionType.NOTIFICATION_LISTENER in missingPermissions) {
                    append("• 通知访问权限\n")
                }
                if (PermissionManager.PermissionType.POST_NOTIFICATIONS in missingPermissions) {
                    append("• 通知权限\n")
                }
                append("\n请在设置中授予这些权限。")
            })
        },
        confirmButton = {
            Button(onClick = onRequestPermissions) {
                Text("前往设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
