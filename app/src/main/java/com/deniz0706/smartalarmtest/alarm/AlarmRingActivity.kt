package com.deniz0706.smartalarmtest.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.*
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.smartalarmtest.model.Alarm
import com.deniz0706.smartalarmtest.model.ChallengeType
import com.deniz0706.smartalarmtest.ui.theme.SmartAlarmTheme
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

class AlarmRingActivity : ComponentActivity() {
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private lateinit var alarm: Alarm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarm = intent.getStringExtra("alarm")?.let { runCatching { Json.decodeFromString<Alarm>(it) }.getOrNull() } ?: run { finish(); return }
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        else window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        player = MediaPlayer().apply { setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()); setDataSource(this@AlarmRingActivity, Settings.System.DEFAULT_ALARM_ALERT_URI); isLooping = true; prepare(); start() }
        if (alarm.vibrate) { vibrator = getSystemService(Vibrator::class.java); vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 600, 400), 0)) }
        setContent { SmartAlarmTheme(darkTheme = true) { RingScreen(alarm, ::dismiss, ::snooze) } }
    }
    private fun dismiss() { player?.stop(); vibrator?.cancel(); AlarmNotifier.cancel(this, alarm.id); finish() }
    private fun snooze() {
        val intent = Intent(this, AlarmReceiver::class.java).putExtra("alarm", Json.encodeToString(alarm))
        val pending = PendingIntent.getBroadcast(this, alarm.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + alarm.snoozeMinutes * 60_000L, pending)
        dismiss()
    }
    override fun onDestroy() { player?.release(); vibrator?.cancel(); super.onDestroy() }
}

@Composable private fun RingScreen(alarm: Alarm, dismiss: () -> Unit, snooze: () -> Unit) {
    var challengeOpen by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primary.copy(.22f), MaterialTheme.colorScheme.background)))) {
        Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(1f)); Text("UYANMA ZAMANI", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("%02d:%02d".format(alarm.hour, alarm.minute), fontSize = 78.sp, fontWeight = FontWeight.Light)
            Text(alarm.label.ifBlank { "Günaydın" }, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.weight(1f))
            AnimatedContent(challengeOpen, label = "challenge") { open ->
                if (open) ChallengePanel(alarm.challenge, dismiss)
                else Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { if (alarm.challenge == ChallengeType.NORMAL) dismiss() else challengeOpen = true }, Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(20.dp)) {
                        Icon(Icons.Rounded.AlarmOff, null); Spacer(Modifier.width(10.dp)); Text(if (alarm.challenge == ChallengeType.NORMAL) "Alarmı kapat" else "${alarm.challenge.title} görevini başlat")
                    }
                    if (alarm.snoozeEnabled) OutlinedButton(onClick = snooze, Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(20.dp)) { Icon(Icons.Rounded.Snooze, null); Spacer(Modifier.width(8.dp)); Text("${alarm.snoozeMinutes} dakika ertele") }
                }
            }
        }
    }
}

@Composable private fun ChallengePanel(type: ChallengeType, complete: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }; var input by remember { mutableStateOf("") }; var error by remember { mutableStateOf(false) }
    val questions = remember { List(3) { val a=Random.nextInt(4,20); val b=Random.nextInt(2,12); Triple(a,b,a+b) } }
    val sequence = remember { List(4) { Random.nextInt(1,10) }.joinToString("") }; var revealed by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { if(type==ChallengeType.MEMORY) { kotlinx.coroutines.delay(2800); revealed=false } }
    Surface(shape=RoundedCornerShape(28.dp), tonalElevation=4.dp) { Column(Modifier.padding(24.dp), horizontalAlignment=Alignment.CenterHorizontally) {
        Text(type.title, style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold); Spacer(Modifier.height(10.dp))
        when(type) {
            ChallengeType.MATH -> { val q=questions[step]; Text("${q.first} + ${q.second} = ?", fontSize=34.sp); OutlinedTextField(input,{input=it.filter(Char::isDigit);error=false}, isError=error, label={Text("Yanıt")})
                Button(onClick={if(input.toIntOrNull()==q.third){if(step==2)complete() else {step++;input=""}}else error=true}){Text("Kontrol et · ${step+1}/3")} }
            ChallengeType.MEMORY -> { Text(if(revealed) sequence else "Diziyi gir", fontSize=36.sp); if(!revealed){OutlinedTextField(input,{input=it.filter(Char::isDigit);error=false},isError=error);Button(onClick={if(input==sequence)complete() else error=true}){Text("Doğrula")}} }
            ChallengeType.QR -> { Text("QR tarama prototipte güvenli yedek akışla çalışır. Alarmı kapatmak için onayla.", textAlign=TextAlign.Center); Button(onClick=complete){Text("QR doğrulamasını tamamla")}}
            else -> Button(onClick=complete){Text("Kapat")}
        }
        if(error) Text("Tekrar dene", color=MaterialTheme.colorScheme.error)
    }}
}
