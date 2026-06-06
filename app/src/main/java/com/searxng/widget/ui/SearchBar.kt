package com.searxng.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

@Composable
fun SearchBar(isDark: Boolean, isCompact: Boolean = false) {
    val bgColor = if (isDark) Color(0xFF3C3C3C) else Color(0xFFF0F0F0)
    val textColor = if (isDark) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(bgColor)
            .cornerRadius(24.dp)
            .padding(horizontal = if (isCompact) 12.dp else 20.dp, vertical = 14.dp)
            .clickable(
                actionStartActivity<SearchActivity>()
            ),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        if (!isCompact) {
            Text(
                text = "\uD83D\uDD0D",
                style = TextStyle(fontSize = 16.sp)
            )
            Spacer(modifier = GlanceModifier.width(10.dp))
        }
        Text(
            text = if (isCompact) "SearXNG" else "Search with SearXNG\u2026",
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = 13.sp
            ),
        )
        Spacer(modifier = GlanceModifier.width(6.dp))
        Text(
            text = "\u25B6",
            style = TextStyle(
                color = ColorProvider(Color(0xFF0057B7)),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        )
    }
}
