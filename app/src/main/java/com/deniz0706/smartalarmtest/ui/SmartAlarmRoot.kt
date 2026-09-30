@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.deniz0706.smartalarmtest.ui

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deniz0706.smartalarmtest.alarm.AlarmScheduler
import com.deniz0706.smartalarmtest.model.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private sealed interface Screen { data object Home:Screen; data class Edit(val alarm:Alarm?):Screen; data object Settings:Screen }

@Composable fun SmartAlarmRoot(vm:AlarmViewModel){
    var screen:Screen by remember{mutableStateOf(Screen.Home)}
    AnimatedContent(screen,label="navigation") { current -> when(current){
        Screen.Home -> HomeScreen(vm,{screen=Screen.Edit(it)},{screen=Screen.Settings})
        is Screen.Edit -> EditScreen(current.alarm,{screen=Screen.Home},{vm.save(it);screen=Screen.Home},{vm.delete(it);screen=Screen.Home})
        Screen.Settings -> SettingsScreen{screen=Screen.Home}
    }}
}

@Composable private fun HomeScreen(vm:AlarmViewModel,onOpen:(Alarm?)->Unit,onSettings:()->Unit){
    val alarms by vm.alarms.collectAsState(); val next=alarms.filter{it.enabled}.minByOrNull{AlarmScheduler.nextOccurrence(it).toInstant()}
    Scaffold(topBar={TopAppBar(title={Column{Text("SmartAlarm",fontWeight=FontWeight.Bold);Text("Gününü zamanında başlat",style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}},actions={IconButton(onClick=onSettings){Icon(Icons.Rounded.Settings,null)}})},floatingActionButton={ExtendedFloatingActionButton(onClick={onOpen(null)},icon={Icon(Icons.Rounded.AddAlarm,null)},text={Text("Alarm ekle")})}){padding->
        LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            if(next!=null)item{NextAlarmCard(next)}
            if(alarms.isEmpty())item{EmptyState{onOpen(null)}} else items(alarms,key={it.id}){alarm->AlarmCard(alarm,{onOpen(alarm)}){vm.save(alarm.copy(enabled=it))}}
            item{Spacer(Modifier.height(80.dp))}
        }
    }
}
@Composable private fun NextAlarmCard(alarm:Alarm){val next=AlarmScheduler.nextOccurrence(alarm);Surface(color=MaterialTheme.colorScheme.primaryContainer,shape=RoundedCornerShape(22.dp)){Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){Icon(Icons.Rounded.NightsStay,null);Spacer(Modifier.width(14.dp));Column{Text("Sıradaki alarm",style=MaterialTheme.typography.labelLarge);Text(next.format(DateTimeFormatter.ofPattern("EEEE, HH:mm",Locale("tr"))).replaceFirstChar{it.uppercase()},style=MaterialTheme.typography.titleMedium)}}}}
@Composable private fun AlarmCard(alarm:Alarm,onClick:()->Unit,onToggle:(Boolean)->Unit){Surface(Modifier.fillMaxWidth().clickable(onClick=onClick),shape=RoundedCornerShape(26.dp),color=MaterialTheme.colorScheme.surfaceVariant.copy(alpha=.7f)){Row(Modifier.padding(20.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text("%02d:%02d".format(alarm.hour,alarm.minute),fontSize=42.sp,fontWeight=FontWeight.Light,color=if(alarm.enabled)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant);if(alarm.label.isNotBlank())Text(alarm.label,fontWeight=FontWeight.SemiBold);Text("${alarm.repeatSummary()}  ·  ${alarm.challenge.title}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Switch(alarm.enabled,onToggle)}}}
@Composable private fun EmptyState(add:()->Unit){Column(Modifier.fillMaxWidth().padding(vertical=80.dp),horizontalAlignment=Alignment.CenterHorizontally){Surface(shape=RoundedCornerShape(30.dp),color=MaterialTheme.colorScheme.surfaceVariant){Icon(Icons.Rounded.AlarmAdd,null,Modifier.padding(24.dp).size(42.dp),tint=MaterialTheme.colorScheme.primary)};Spacer(Modifier.height(20.dp));Text("Sabahların henüz sessiz",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text("İlk alarmını oluştur ve güne kendi ritminde başla.",Modifier.padding(12.dp),color=MaterialTheme.colorScheme.onSurfaceVariant);TextButton(onClick=add){Text("İlk alarmı oluştur")}}}

@Composable private fun EditScreen(existing:Alarm?,back:()->Unit,save:(Alarm)->Unit,delete:(Alarm)->Unit){
    var alarm by remember{mutableStateOf(existing?.takeIf { it.challenge.available } ?: existing?.copy(challenge=ChallengeType.NORMAL) ?: Alarm())};val context=LocalContext.current
    Scaffold(topBar={TopAppBar(title={Text(if(existing==null)"Yeni alarm" else "Alarmı düzenle")},navigationIcon={IconButton(onClick=back){Icon(Icons.Rounded.ArrowBack,null)}},actions={TextButton(onClick={save(alarm)}){Text("Kaydet")}})}){padding->
        LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
            item{Column(Modifier.fillMaxWidth().clickable{TimePickerDialog(context,{_,h,m->alarm=alarm.copy(hour=h,minute=m)},alarm.hour,alarm.minute,true).show()},horizontalAlignment=Alignment.CenterHorizontally){Text("%02d:%02d".format(alarm.hour,alarm.minute),fontSize=68.sp,fontWeight=FontWeight.Light);Text("Değiştirmek için dokun",color=MaterialTheme.colorScheme.primary)}}
            item{OutlinedTextField(alarm.label,{alarm=alarm.copy(label=it)},Modifier.fillMaxWidth(),label={Text("Alarm adı")},placeholder={Text("Örn. Sabah koşusu")},singleLine=true,leadingIcon={Icon(Icons.Rounded.Label,null)})}
            item{Section("Tekrar"){DaySelector(alarm.repeatDays){alarm=alarm.copy(repeatDays=it)}}}
            item{Section("Alarm seçenekleri"){SettingSwitch("Alarm etkin","Kaydedildiğinde planlanır",alarm.enabled){alarm=alarm.copy(enabled=it)};SettingSwitch("Titreşim","Sesle birlikte titreşim",alarm.vibrate){alarm=alarm.copy(vibrate=it)};SettingSwitch("Erteleme","Alarmı kısa süreliğine ertele",alarm.snoozeEnabled){alarm=alarm.copy(snoozeEnabled=it)};if(alarm.snoozeEnabled){Text("Erteleme süresi: ${alarm.snoozeMinutes} dakika");Slider(alarm.snoozeMinutes.toFloat(),{alarm=alarm.copy(snoozeMinutes=it.toInt())},valueRange=5f..30f,steps=4)}}}
            item{Section("Kapatma görevi"){ChallengeType.entries.forEach{type->ChallengeRow(type,alarm.challenge==type){alarm=alarm.copy(challenge=type)}}}}
            if(existing!=null)item{OutlinedButton(onClick={delete(existing)},Modifier.fillMaxWidth(),colors=ButtonDefaults.outlinedButtonColors(contentColor=MaterialTheme.colorScheme.error)){Icon(Icons.Rounded.Delete,null);Spacer(Modifier.width(8.dp));Text("Alarmı sil")}}
            item{Spacer(Modifier.height(20.dp))}
        }
    }
}
@Composable private fun Section(title:String,content:@Composable ColumnScope.()->Unit){Surface(shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surfaceVariant.copy(.55f)){Column(Modifier.fillMaxWidth().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text(title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold);content()}}}
@Composable private fun DaySelector(selected:Set<Int>,onChange:(Set<Int>)->Unit){Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){(1..7).forEach{day->FilterChip(day in selected,{onChange(if(day in selected)selected-day else selected+day)},label={Text(java.time.DayOfWeek.of(day).shortTurkish())})}}}
@Composable private fun SettingSwitch(title:String,subtitle:String,checked:Boolean,onChange:(Boolean)->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(title);Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)};Switch(checked,onChange)}}
@Composable private fun ChallengeRow(type:ChallengeType,selected:Boolean,onClick:()->Unit){Surface(Modifier.fillMaxWidth().clickable(enabled=type.available,onClick=onClick),shape=RoundedCornerShape(18.dp),color=if(selected)MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){RadioButton(selected,onClick=if(type.available)onClick else null,enabled=type.available);Column{Text(type.title,fontWeight=FontWeight.SemiBold,color=if(type.available)MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant);Text(type.description,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}}}}

@Composable private fun SettingsScreen(back:()->Unit){val context=LocalContext.current;Scaffold(topBar={TopAppBar(title={Text("Ayarlar ve hakkında")},navigationIcon={IconButton(onClick=back){Icon(Icons.Rounded.ArrowBack,null)}})}){padding->Column(Modifier.padding(padding).padding(20.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){Section("Sistem izinleri"){Text("Kesin alarm izni, alarmın tam zamanında çalmasını sağlar.",color=MaterialTheme.colorScheme.onSurfaceVariant);Button(onClick={if(Build.VERSION.SDK_INT>=31) context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))) else context.startActivity(Intent(Settings.ACTION_SETTINGS))}){Text("Kesin alarm iznini yönet")}};Section("SmartAlarm Test"){Text("Görevsiz yeniden uyumaya karşı tasarlanan deneysel, modern alarm prototipi.");Text("Sürüm 1.0",style=MaterialTheme.typography.labelMedium)}}}}
