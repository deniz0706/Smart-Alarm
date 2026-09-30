package com.deniz0706.smartalarmtest.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Dark = darkColorScheme(primary=Color(0xFFB9C4FF), secondary=Color(0xFF86D6C5), background=Color(0xFF101114), surface=Color(0xFF191A1F), surfaceVariant=Color(0xFF23252C))
private val Light = lightColorScheme(primary=Color(0xFF4054A5), secondary=Color(0xFF006B5B), background=Color(0xFFF9F8FE), surface=Color.White)
@Composable
fun SmartAlarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) = MaterialTheme(colorScheme = if (darkTheme) Dark else Light, typography = Typography(), content = content)
