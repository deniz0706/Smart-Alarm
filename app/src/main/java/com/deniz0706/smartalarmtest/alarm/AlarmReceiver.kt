package com.deniz0706.smartalarmtest.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.deniz0706.smartalarmtest.model.Alarm
import kotlinx.serialization.json.Json

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val raw = intent.getStringExtra("alarm") ?: return
        val alarm = runCatching { Json.decodeFromString<Alarm>(raw) }.getOrNull() ?: return
        AlarmNotifier.show(context, alarm)
        if (alarm.repeatDays.isNotEmpty()) AlarmScheduler.schedule(context, alarm)
    }
}
