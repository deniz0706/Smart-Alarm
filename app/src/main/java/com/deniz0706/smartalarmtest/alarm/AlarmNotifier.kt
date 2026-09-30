package com.deniz0706.smartalarmtest.alarm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.deniz0706.smartalarmtest.R
import com.deniz0706.smartalarmtest.model.Alarm
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AlarmNotifier {
    const val CHANNEL = "ringing_alarms"
    fun createChannel(context: Context) {
        val sound = Settings.System.DEFAULT_ALARM_ALERT_URI
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Çalan alarmlar", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alarm çaldığında tam ekran bildirimler"; setSound(sound, attrs); enableVibration(true); lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            })
    }
    fun show(context: Context, alarm: Alarm) {
        val intent = Intent(context, AlarmRingActivity::class.java).putExtra("alarm", Json.encodeToString(alarm)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val fullScreen = PendingIntent.getActivity(context, alarm.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL)
            .setSmallIcon(R.drawable.ic_alarm).setContentTitle(alarm.label.ifBlank { "Alarm" })
            .setContentText("${alarm.hour.toString().padStart(2,'0')}:${alarm.minute.toString().padStart(2,'0')}")
            .setCategory(NotificationCompat.CATEGORY_ALARM).setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true).setFullScreenIntent(fullScreen, true).setContentIntent(fullScreen).build()
        context.getSystemService(NotificationManager::class.java).notify(alarm.id.hashCode(), notification)
        context.startActivity(intent)
    }
    fun cancel(context: Context, id: Long) = context.getSystemService(NotificationManager::class.java).cancel(id.hashCode())
}
