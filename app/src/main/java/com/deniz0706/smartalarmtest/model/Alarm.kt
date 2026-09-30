package com.deniz0706.smartalarmtest.model

import kotlinx.serialization.Serializable
import java.time.DayOfWeek

@Serializable
enum class ChallengeType(val title: String, val description: String, val available: Boolean = true) {
    NORMAL("Normal", "Tek dokunuşla alarmı kapat"),
    MATH("Matematik", "Üç işlemi doğru çözerek uyan"),
    MEMORY("Hafıza", "Gösterilen diziyi hatırla"),
    QR("QR Kod · Yakında", "Kamera ile QR doğrulaması bu sürümde kullanılamıyor", false)
}

@Serializable
data class Alarm(
    val id: Long = System.currentTimeMillis(),
    val hour: Int = 7,
    val minute: Int = 0,
    val label: String = "",
    val enabled: Boolean = true,
    val repeatDays: Set<Int> = emptySet(), // DayOfWeek values: Monday=1
    val vibrate: Boolean = true,
    val snoozeEnabled: Boolean = true,
    val snoozeMinutes: Int = 10,
    val challenge: ChallengeType = ChallengeType.NORMAL
) {
    fun repeatSummary(): String = when {
        repeatDays.isEmpty() -> "Bir kez"
        repeatDays.size == 7 -> "Her gün"
        repeatDays == setOf(1, 2, 3, 4, 5) -> "Hafta içi"
        repeatDays == setOf(6, 7) -> "Hafta sonu"
        else -> repeatDays.sorted().joinToString(" · ") { DayOfWeek.of(it).shortTurkish() }
    }
}

fun DayOfWeek.shortTurkish() = listOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")[value - 1]
