package com.deniz0706.smartalarmtest

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.deniz0706.smartalarmtest.ui.SmartAlarmRoot
import com.deniz0706.smartalarmtest.ui.AlarmViewModel
import com.deniz0706.smartalarmtest.ui.AlarmViewModelFactory
import com.deniz0706.smartalarmtest.ui.theme.SmartAlarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) { super.onCreate(savedInstanceState); setContent {
        SmartAlarmTheme { val vm: AlarmViewModel = viewModel(factory=AlarmViewModelFactory((application as SmartAlarmApp).repository, applicationContext)); NotificationPermission(); SmartAlarmRoot(vm) }
    }}
    @Composable private fun NotificationPermission() {
        val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){}
        LaunchedEffect(Unit){if(Build.VERSION.SDK_INT>=33) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)}
    }
}
