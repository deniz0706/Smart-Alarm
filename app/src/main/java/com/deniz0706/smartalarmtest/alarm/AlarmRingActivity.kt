package com.deniz0706.smartalarmtest.alarm

import android.content.Intent
import android.os.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import kotlinx.serialization.json.Json
import kotlin.random.Random

class AlarmRingActivity : ComponentActivity() {
    private lateinit var alarm: Alarm
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        alarm = intent.getStringExtra("alarm")?.let { runCatching { Json.decodeFromString<Alarm>(it) }.getOrNull() } ?: run { finish(); return }
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        else window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        setContent { SmartAlarmTheme(darkTheme = true) { RingScreen(alarm, ::dismiss, ::snooze) } }
    }
    private fun dismiss() { stopService(Intent(this, AlarmRingingService::class.java)); AlarmNotifier.cancel(this, alarm.id); finishAndRemoveTask() }
    private fun snooze() {
        AlarmScheduler.scheduleSnooze(this, alarm)
        dismiss()
    }
}

@Composable private fun RingScreen(alarm: Alarm, dismiss: () -> Unit, snooze: () -> Unit) {
    BackHandler(enabled = true) { /* Alarm exits only through snooze or a completed challenge. */ }
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
    val questions = remember { List(3) { MathQuestion.beginner() } }
    val sequence = remember { List(4) { Random.nextInt(1,10) }.joinToString("") }; var revealed by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { if(type==ChallengeType.MEMORY) { kotlinx.coroutines.delay(2800); revealed=false } }
    Surface(shape=RoundedCornerShape(28.dp), tonalElevation=4.dp) { Column(Modifier.padding(24.dp), horizontalAlignment=Alignment.CenterHorizontally) {
        Text(type.title, style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold); Spacer(Modifier.height(10.dp))
        when(type) {
            ChallengeType.MATH -> { val q=questions[step]; Text(q.prompt, fontSize=34.sp); OutlinedTextField(input,{input=it.filter(Char::isDigit);error=false}, isError=error, label={Text("Yanıt")})
                Button(onClick={if(input.toIntOrNull()==q.answer){if(step==2)complete() else {step++;input=""}}else error=true}){Text("Kontrol et · ${step+1}/3")} }
            ChallengeType.MEMORY -> { Text(if(revealed) sequence else "Diziyi gir", fontSize=36.sp); if(!revealed){OutlinedTextField(input,{input=it.filter(Char::isDigit);error=false},isError=error);Button(onClick={if(input==sequence)complete() else error=true}){Text("Doğrula")}} }
            ChallengeType.QR -> { Text("QR görevi bu sürümde kullanılamıyor. Bu, daha önce kaydedilmiş bir önizleme alarmıdır.", textAlign=TextAlign.Center); Button(onClick=complete){Text("Alarmı kapat")}}
            else -> Button(onClick=complete){Text("Kapat")}
        }
        if(error) Text("Tekrar dene", color=MaterialTheme.colorScheme.error)
    }}
}

internal data class MathQuestion(val prompt: String, val answer: Int) {
    companion object {
        fun beginner(random: Random = Random.Default): MathQuestion = when (random.nextInt(3)) {
            0 -> { val a=random.nextInt(3,31); val b=random.nextInt(2,21); MathQuestion("$a + $b = ?",a+b) }
            1 -> { val answer=random.nextInt(1,21); val b=random.nextInt(2,21); MathQuestion("${answer+b} − $b = ?",answer) }
            else -> { val a=random.nextInt(2,10); val b=random.nextInt(2,10); MathQuestion("$a × $b = ?",a*b) }
        }
    }
}
