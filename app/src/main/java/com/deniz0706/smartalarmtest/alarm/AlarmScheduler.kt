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
    const val ACTION_ALARM = "com.deniz0706.smartalarmtest.action.ALARM"
    const val ACTION_SNOOZE = "com.deniz0706.smartalarmtest.action.SNOOZE"
    internal fun alarmRequestCode(id: Long) = id.hashCode()
    internal fun snoozeRequestCode(id: Long) = id.hashCode() xor 0x5A00_0000
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
    fun scheduleSnooze(context: Context, alarm: Alarm) {
        val trigger = System.currentTimeMillis() + alarm.snoozeMinutes * 60_000L
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, trigger, pendingIntent(context, alarm, true)
        )
    }
    fun cancel(context: Context, id: Long) {
        val manager = context.getSystemService(AlarmManager::class.java)
        regularPendingIntent(context, id, PendingIntent.FLAG_NO_CREATE)?.let(manager::cancel)
        snoozePendingIntent(context, id, PendingIntent.FLAG_NO_CREATE)?.let(manager::cancel)
    }
    private fun pendingIntent(context: Context, alarm: Alarm) = PendingIntent.getBroadcast(
        context, alarmRequestCode(alarm.id), Intent(context, AlarmReceiver::class.java).setAction(ACTION_ALARM).putExtra("alarm", Json.encodeToString(alarm)),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    private fun pendingIntent(context: Context, alarm: Alarm, snooze: Boolean) = PendingIntent.getBroadcast(
        context, snoozeRequestCode(alarm.id), Intent(context, AlarmReceiver::class.java).setAction(ACTION_SNOOZE).putExtra("alarm", Json.encodeToString(alarm)),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    private fun regularPendingIntent(context: Context, id: Long, flags: Int) = PendingIntent.getBroadcast(
        context, alarmRequestCode(id), Intent(context, AlarmReceiver::class.java).setAction(ACTION_ALARM), flags or PendingIntent.FLAG_IMMUTABLE
    )
    private fun snoozePendingIntent(context: Context, id: Long, flags: Int) = PendingIntent.getBroadcast(
        context, snoozeRequestCode(id), Intent(context, AlarmReceiver::class.java).setAction(ACTION_SNOOZE), flags or PendingIntent.FLAG_IMMUTABLE
    )
}
