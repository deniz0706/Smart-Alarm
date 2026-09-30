package com.deniz0706.smartalarmtest.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.deniz0706.smartalarmtest.model.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore("smart_alarm_data")

class AlarmRepository(private val context: Context) {
    private val key = stringPreferencesKey("alarms")
    private val json = Json { ignoreUnknownKeys = true }
    val alarms: Flow<List<Alarm>> = context.dataStore.data.map { prefs ->
        runCatching { json.decodeFromString<List<Alarm>>(prefs[key] ?: "[]") }.getOrDefault(emptyList())
    }
    suspend fun save(alarm: Alarm) = context.dataStore.edit { prefs ->
        val current = runCatching { json.decodeFromString<List<Alarm>>(prefs[key] ?: "[]") }.getOrDefault(emptyList())
        prefs[key] = json.encodeToString((current.filterNot { it.id == alarm.id } + alarm).sortedWith(compareBy({ it.hour }, { it.minute })))
    }
    suspend fun delete(id: Long) = context.dataStore.edit { prefs ->
        val current = runCatching { json.decodeFromString<List<Alarm>>(prefs[key] ?: "[]") }.getOrDefault(emptyList())
        prefs[key] = json.encodeToString(current.filterNot { it.id == id })
    }
}
