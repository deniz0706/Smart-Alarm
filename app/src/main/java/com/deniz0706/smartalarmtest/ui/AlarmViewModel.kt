package com.deniz0706.smartalarmtest.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.deniz0706.smartalarmtest.alarm.AlarmScheduler
import com.deniz0706.smartalarmtest.data.AlarmRepository
import com.deniz0706.smartalarmtest.model.Alarm
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(private val repo:AlarmRepository, private val context:Context):ViewModel(){
    val alarms=repo.alarms.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun save(alarm:Alarm)=viewModelScope.launch{repo.save(alarm);AlarmScheduler.schedule(context,alarm)}
    fun delete(alarm:Alarm)=viewModelScope.launch{repo.delete(alarm.id);AlarmScheduler.cancel(context,alarm.id)}
}
class AlarmViewModelFactory(private val repo:AlarmRepository,private val context:Context):ViewModelProvider.Factory{
    override fun <T:ViewModel> create(modelClass:Class<T>):T { @Suppress("UNCHECKED_CAST") return AlarmViewModel(repo,context) as T }
}
