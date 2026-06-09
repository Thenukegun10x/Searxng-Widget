package com.searxng.widget.ui.theme

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding

object WidgetColors {
    val primary = Color(0xFF0057B7)
    val primaryDark = Color(0xFF4FC3F7)
    val accentLight = Color(0xFF3050FF)
    val accentDark = Color(0xFF5588FF)
    val backgroundLight = Color(0xFFFFFFFF)
    val backgroundDark = Color(0xFF1C1B1F)
    val surfaceLight = Color(0xFFF5F5F5)
    val surfaceDark = Color(0xFF2C2C2C)
    val onSurfaceLight = Color(0xFF1C1B1F)
    val onSurfaceDark = Color(0xFFE6E1E5)
    val textSecondary = Color(0xFF888888)
    val divider = Color(0xFFE0E0E0)
    val dividerDark = Color(0xFF3C3C3C)
}

fun isDarkTheme(configuration: Configuration): Boolean {
    return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

@Composable
fun WidgetBackground(
    isDark: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(if (isDark) WidgetColors.backgroundDark else WidgetColors.backgroundLight)
            .padding(12.dp),
        content = content
    )
}
