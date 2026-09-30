package com.deniz0706.smartalarmtest.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.deniz0706.smartalarmtest.SmartAlarmApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED && intent.action != Intent.ACTION_MY_PACKAGE_REPLACED) return
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try { (context.applicationContext as SmartAlarmApp).repository.alarms.first().filter { it.enabled }.forEach { AlarmScheduler.schedule(context, it) } }
            finally { result.finish() }
        }
    }
}
