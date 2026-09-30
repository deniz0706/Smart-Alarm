package com.deniz0706.smartalarmtest.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.deniz0706.smartalarmtest.SmartAlarmApp
import com.deniz0706.smartalarmtest.model.Alarm
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val raw = intent.getStringExtra("alarm") ?: return
        val alarm = runCatching { Json.decodeFromString<Alarm>(raw) }.getOrNull() ?: return
        ContextCompat.startForegroundService(
            context,
            Intent(context, AlarmRingingService::class.java).putExtra("alarm", raw)
        )
        // A snooze is only an additional occurrence; it must never alter the normal repeat chain.
        if (intent.action == AlarmScheduler.ACTION_SNOOZE) return
        if (alarm.repeatDays.isNotEmpty()) {
            AlarmScheduler.schedule(context, alarm)
        } else {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    (context.applicationContext as SmartAlarmApp).repository.save(alarm.copy(enabled = false))
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
