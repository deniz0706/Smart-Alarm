package com.deniz0706.smartalarmtest.alarm

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import com.deniz0706.smartalarmtest.model.Alarm
import kotlinx.serialization.json.Json

/** Owns ringing independently from the full-screen activity.
 * This keeps the alarm audible and reachable through its notification when Android declines FSI. */
class AlarmRingingService : Service() {
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var currentId: Long? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val alarm = intent?.getStringExtra("alarm")
            ?.let { runCatching { Json.decodeFromString<Alarm>(it) }.getOrNull() }
            ?: return START_NOT_STICKY.also { stopSelf() }
        stopOutput()
        currentId?.takeIf { it != alarm.id }?.let { AlarmNotifier.cancel(this, it) }
        currentId = alarm.id
        startForeground(alarm.id.hashCode(), AlarmNotifier.notification(this, alarm))
        player = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
                setDataSource(this@AlarmRingingService, Settings.System.DEFAULT_ALARM_ALERT_URI)
                isLooping = true
                setOnErrorListener { _, _, _ -> stopOutput(); true }
                prepare()
                start()
            }
        }.getOrNull()
        if (alarm.vibrate) {
            vibrator = getSystemService(Vibrator::class.java)
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 600, 400), 0))
        }
        return START_NOT_STICKY
    }

    private fun stopOutput() {
        player?.runCatching { if (isPlaying) stop() }
        player?.release()
        player = null
        vibrator?.cancel()
        vibrator = null
    }

    override fun onDestroy() { stopOutput(); currentId?.let { AlarmNotifier.cancel(this, it) }; super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null
}
