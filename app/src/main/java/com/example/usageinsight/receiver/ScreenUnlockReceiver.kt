package com.example.usageinsight.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.usageinsight.UsageInsightApp
import com.example.usageinsight.data.entity.UnlockEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ScreenUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            val app = context.applicationContext as UsageInsightApp
            
            CoroutineScope(Dispatchers.IO).launch {
                app.unlockDao.insert(
                    UnlockEntity(
                        timestamp = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
