package com.searxng.widget.ui

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.SslErrorHandler
import android.net.http.SslError
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.searxng.widget.R
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.viewinterop.AndroidView
import com.searxng.widget.preferences.WidgetPrefs

class SearchOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        window.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
        window.decorView.setBackgroundColor(AndroidColor.TRANSPARENT)
        super.onCreate(savedInstanceState)

        setContent {
            var instanceUrl by remember { mutableStateOf<String?>(null) }
            var themeMode by remember { mutableStateOf(WidgetPrefs.ThemeMode.SYSTEM) }
            var loaded by remember { mutableStateOf(false) }
            var showResults by remember { mutableStateOf(false) }
            var searchUrl by remember { mutableStateOf("") }
            var query by remember { mutableStateOf("") }
            var selectedCategory by remember { mutableStateOf("general") }

            LaunchedEffect(Unit) {
                val prefs = WidgetPrefs(this@SearchOverlayActivity)
                instanceUrl = prefs.getInstanceUrl()
                themeMode = prefs.getThemeMode()
                loaded = true
            }

            val isDark = when (themeMode) {
                WidgetPrefs.ThemeMode.LIGHT -> false
                WidgetPrefs.ThemeMode.DARK -> true
                WidgetPrefs.ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            if (!loaded) {
                val accent = if (isDark) Color(0xFF5588FF) else Color(0xFF3050FF)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(if (isDark) Color(0xE61E1E22) else Color(0xE6F2F5F8)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = accent,
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 2.dp
                    )
                }
            } else if (instanceUrl == null) {
                LaunchedEffect(Unit) { finish() }
            } else if (showResults) {
                SearxngResultsView(
                    searchUrl = searchUrl,
                    isDark = isDark,
                    onBack = { showResults = false },
                    instanceUrl = instanceUrl
                )
            } else {
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
                    SearchOverlayContent(
                        instanceUrl = instanceUrl!!,
                        isDark = isDark,
                        query = query,
                        onQueryChange = { query = it },
                        selectedCategory = selectedCategory,
                        onCategoryChange = { selectedCategory = it },
                        onSearch = { q, cat ->
                            val baseUrl = instanceUrl!!.trimEnd('/')
                            searchUrl = "$baseUrl/search?q=${Uri.encode(q)}&categories=$cat"
                            showResults = true
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
    query: String,
    onQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    onSearch: (query: String, category: String) -> Unit
) {
    val searchBg = if (isDark) Color(0xFF2B2E36) else Color(0xFFFFFFFF)
    val searchText = if (isDark) Color(0xFFFFFFFF) else Color(0xFF222222)
    val borderColor = if (isDark) Color(0xFF555555) else Color(0xFFBBBBBB)
    val accent = if (isDark) Color(0xFF5588FF) else Color(0xFF3050FF)
    val placeholderColor = Color(0xFF888888)
    val titleColor = if (isDark) Color.White else Color.Black

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
                    text = stringResource(R.string.app_name),
                    color = titleColor,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            placeholder = {
                Text(stringResource(R.string.search_for), color = placeholderColor, fontSize = 14.sp)
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
                    onClick = { onCategoryChange(category) }
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

@Composable
fun SearxngResultsView(
    searchUrl: String,
    isDark: Boolean,
    onBack: () -> Unit,
    instanceUrl: String? = null
) {
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableIntStateOf(0) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    val bgColor = if (isDark) Color(0xFF1C1B1F) else Color(0xFFFAFAFA)

    BackHandler(enabled = true) {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            progress = 0
                            isLoading = true
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            isLoading = false
                        }

                        @Suppress("DEPRECATION")
                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            isLoading = false
                        }

                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: SslError?
                        ) {
                            handler?.cancel()
                            isLoading = false
                        }
                    }
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            progress = newProgress
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    if (isDark && Build.VERSION.SDK_INT >= 29) {
                        @Suppress("DEPRECATION")
                        settings.forceDark = WebSettings.FORCE_DARK_ON
                    }
                    setBackgroundColor(
                        if (isDark) AndroidColor.BLACK else AndroidColor.WHITE
                    )
                    loadUrl(searchUrl)
                    webView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading && progress > 0) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                color = if (isDark) Color(0xFF5588FF) else Color(0xFF3050FF),
                trackColor = Color.Transparent
            )
        }
    }
}
