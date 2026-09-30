package com.deniz0706.smartalarmtest.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.deniz0706.smartalarmtest.model.Alarm
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.*

object AlarmScheduler {
    fun nextOccurrence(alarm: Alarm, now: ZonedDateTime = ZonedDateTime.now()): ZonedDateTime {
        val today = now.withHour(alarm.hour).withMinute(alarm.minute).withSecond(0).withNano(0)
        if (alarm.repeatDays.isEmpty()) return if (today.isAfter(now)) today else today.plusDays(1)
        return (0L..7L).map { today.plusDays(it) }
            .first { it.isAfter(now) && it.dayOfWeek.value in alarm.repeatDays }
    }
    fun schedule(context: Context, alarm: Alarm) {
        cancel(context, alarm.id)
        if (!alarm.enabled) return
        val manager = context.getSystemService(AlarmManager::class.java)
        val pending = pendingIntent(context, alarm)
        val trigger = nextOccurrence(alarm).toInstant().toEpochMilli()
        if (Build.VERSION.SDK_INT < 31 || manager.canScheduleExactAlarms())
            manager.setAlarmClock(AlarmManager.AlarmClockInfo(trigger, pending), pending)
        else manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, trigger, pending)
    }
    fun cancel(context: Context, id: Long) = context.getSystemService(AlarmManager::class.java).cancel(
        PendingIntent.getBroadcast(context, id.hashCode(), Intent(context, AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE)
    )
    private fun pendingIntent(context: Context, alarm: Alarm) = PendingIntent.getBroadcast(
        context, alarm.id.hashCode(), Intent(context, AlarmReceiver::class.java).putExtra("alarm", Json.encodeToString(alarm)),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
