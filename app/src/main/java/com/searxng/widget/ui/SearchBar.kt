package com.searxng.widget.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.background
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.action.actionSetText
import androidx.glance.layout.Align
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.searxng.widget.SearxngWidget

@Composable
fun SearchBar(
    query: String,
    onValueChange: Action,
    onSearch: Action,
    isDark: Boolean
) {
    val bgColor = if (isDark) Color(0xFF3C3C3C) else Color(0xFFF0F0F0)
    val textColor = if (isDark) Color(0xFFE6E1E5) else Color(0xFF1C1B1F)
    val hintColor = if (isDark) Color(0xFF888888) else Color(0xFFAAAAAA)

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(bgColor)
            .cornerRadius(8.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Align.CenterVertically
    ) {
        androidx.glance.text.TextField(
            value = query,
            onValueChange = actionSetText(SearxngWidget.KEY_QUERY),
            placeholder = "Search with SearXNG\u2026",
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = 14.sp
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = "\u25B6",
            style = TextStyle(
                color = ColorProvider(Color(0xFF0057B7)),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            modifier = GlanceModifier.clickable(onSearch)
        )
    }
}
