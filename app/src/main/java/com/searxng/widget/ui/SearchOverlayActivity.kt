package com.searxng.widget.ui

import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.searxng.widget.preferences.WidgetPrefs

class SearchOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
        window.decorView.setBackgroundColor(AndroidColor.TRANSPARENT)
        super.onCreate(savedInstanceState)

        setContent {
            var instanceUrl by remember { mutableStateOf<String?>(null) }
            var loaded by remember { mutableStateOf(false) }
            val isDark = isSystemInDarkTheme()

            LaunchedEffect(Unit) {
                instanceUrl = WidgetPrefs(this@SearchOverlayActivity).getInstanceUrl()
                loaded = true
            }

            val scrimColor = if (isDark) Color(0xE61E1E22) else Color(0xE6F2F5F8)
            val accent = if (isDark) Color(0xFF5588FF) else Color(0xFF3050FF)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scrimColor)
                    .clickable(
                        onClick = { finish() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                if (!loaded) {
                    CircularProgressIndicator(
                        color = accent,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                } else if (instanceUrl == null) {
                    LaunchedEffect(Unit) { finish() }
                } else {
                    SearchOverlayContent(
                        instanceUrl = instanceUrl!!,
                        isDark = isDark,
                        onSearch = { query, category ->
                            val baseUrl = instanceUrl!!.trimEnd('/')
                            val searchUrl = "$baseUrl/search?q=${Uri.encode(query)}&categories=$category"
                            val customTabsIntent = CustomTabsIntent.Builder()
                                .setShowTitle(true)
                                .setColorScheme(
                                    if (isDark) CustomTabsIntent.COLOR_SCHEME_DARK
                                    else CustomTabsIntent.COLOR_SCHEME_LIGHT
                                )
                                .build()
                            customTabsIntent.launchUrl(
                                this@SearchOverlayActivity,
                                Uri.parse(searchUrl)
                            )
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchOverlayContent(
    instanceUrl: String,
    isDark: Boolean,
    onSearch: (query: String, category: String) -> Unit
) {
    val searchBg = if (isDark) Color(0xFF2B2E36) else Color(0xFFFFFFFF)
    val searchText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF222222)
    val borderColor = if (isDark) Color(0xFF555555) else Color(0xFFBBBBBB)
    val accent = if (isDark) Color(0xFF5588FF) else Color(0xFF3050FF)
    val placeholderColor = Color(0xFF888888)
    val titleColor = if (isDark) Color.White else Color.Black

    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("general") }
    val focusRequester = remember { FocusRequester() }
    val categories = listOf(
        "general", "images", "videos", "news",
        "music", "files", "science", "it"
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {},
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "SearXNG",
            color = titleColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = {
                Text("Search for...", color = placeholderColor, fontSize = 14.sp)
            },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(
                        onClick = { onSearch(query, selectedCategory) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "\u25B6",
                            color = accent,
                            fontSize = 16.sp
                        )
                    }
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch(query, selectedCategory) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = searchBg,
                unfocusedContainerColor = searchBg,
                focusedTextColor = searchText,
                unfocusedTextColor = searchText,
                focusedBorderColor = accent,
                unfocusedBorderColor = borderColor,
                cursorColor = accent,
                focusedLeadingIconColor = accent,
                unfocusedLeadingIconColor = accent
            ),
            shape = MaterialTheme.shapes.large,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategoryChip(
                    category = category,
                    isSelected = category == selectedCategory,
                    isDark = isDark,
                    accent = accent,
                    onClick = { selectedCategory = category }
                )
            }
        }

        Text(
            text = instanceUrl,
            color = placeholderColor,
            fontSize = 11.sp
        )
    }
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    isDark: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) accent else if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)
    val textColor = if (isSelected) Color.White else if (isDark) Color(0xFFBBBBBB) else Color(0xFF444444)

    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Text(
            text = category.replaceFirstChar { it.uppercase() },
            color = textColor,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
