package com.deniz0706.smartalarmtest

import android.app.Application
import com.deniz0706.smartalarmtest.alarm.AlarmNotifier
import com.deniz0706.smartalarmtest.data.AlarmRepository

class SmartAlarmApp : Application() {
    val repository by lazy { AlarmRepository(this) }
    override fun onCreate() { super.onCreate(); AlarmNotifier.createChannel(this) }
}
