package com.searxng.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
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
import com.searxng.widget.R
import com.searxng.widget.ui.theme.WidgetColors

@Composable
fun SearchBar(instanceUrl: String, isDark: Boolean, isCompact: Boolean = false) {
    val context = LocalContext.current
    val searchBg = if (isDark) Color(0xFF2B2E36) else WidgetColors.backgroundLight
    val placeholderColor = WidgetColors.textSecondary
    val accent = if (isDark) WidgetColors.accentDark else WidgetColors.accentLight

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(searchBg)
            .cornerRadius(24.dp)
            .padding(
                horizontal = if (isCompact) 12.dp else 16.dp,
                vertical = if (isCompact) 10.dp else 12.dp
            )
            .clickable(
                actionStartActivity<SearchOverlayActivity>()
            ),
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = "\uD83D\uDD0D",
            style = TextStyle(fontSize = 16.sp)
        )
        Spacer(modifier = GlanceModifier.width(8.dp))

        Text(
            text = context.getString(R.string.search_hint),
            style = TextStyle(
                color = ColorProvider(placeholderColor),
                fontSize = if (isCompact) 13.sp else 14.sp
            ),
            modifier = GlanceModifier.defaultWeight()
        )

        Text(
            text = "\u25B6",
            style = TextStyle(
                color = ColorProvider(accent),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        )
    }
}
