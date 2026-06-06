package com.searxng.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.searxng.widget.data.model.SearchResult

@Composable
fun SearchResults(
    results: List<SearchResult>,
    onResultClicked: (String) -> Action,
    isDark: Boolean
) {
    val titleColor = ColorProvider(
        if (isDark) Color(0xFF4FC3F7) else Color(0xFF0057B7)
    )
    val snippetColor = ColorProvider(
        if (isDark) Color(0xFFE6E1E5) else Color(0xFF444444)
    )

    LazyColumn(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        items(results) { result ->
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 4.dp)
                    .clickable(onResultClicked(result.url))
            ) {
                Text(
                    text = result.title,
                    style = TextStyle(
                        color = titleColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        textDecoration = TextDecoration.Underline
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!result.content.isNullOrBlank()) {
                    Spacer(modifier = GlanceModifier.height(2.dp))
                    Text(
                        text = result.content,
                        style = TextStyle(
                            color = snippetColor,
                            fontSize = 12.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
