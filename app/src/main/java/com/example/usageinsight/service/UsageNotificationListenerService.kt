package com.example.usageinsight.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.usageinsight.UsageInsightApp
import com.example.usageinsight.data.entity.NotificationEntity
import com.example.usageinsight.util.getTodayStartMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UsageNotificationListenerService : NotificationListenerService() {
    private val TAG = "NotificationListener"
    private val serviceScope = CoroutineScope(Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "NotificationListenerService created")
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListenerService connected")
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d(TAG, "Notification received from: ${sbn.packageName}")
        serviceScope.launch {
            val notificationDao = (application as UsageInsightApp).database.notificationDao()
            notificationDao.insertNotification(
                NotificationEntity(
                    packageName = sbn.packageName,
                    timestamp = sbn.postTime,
                    date = getTodayStartMillis()
                )
            )
        }
    }
} 