package com.searxng.widget.ui

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.searxng.widget.preferences.WidgetPrefs
import kotlinx.coroutines.runBlocking

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        window.decorView.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        val instanceUrl = runBlocking { WidgetPrefs(this@SearchActivity).getInstanceUrl() } ?: run {
            finish()
            return
        }

        setContent {
            val themeMode = runBlocking { WidgetPrefs(this@SearchActivity).getThemeMode() }
            val isDark = when (themeMode) {
                WidgetPrefs.ThemeMode.LIGHT -> false
                WidgetPrefs.ThemeMode.DARK -> true
                WidgetPrefs.ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            var mode by remember { mutableStateOf<SearchMode>(SearchMode.INPUT) }
            val currentMode = mode

            when (currentMode) {
                SearchMode.INPUT -> SearchOverlay(
                    instanceUrl = instanceUrl,
                    isDark = isDark,
                    onSearch = { query ->
                        mode = SearchMode.RESULTS(query)
                    },
                    onDismiss = { finish() }
                )
                is SearchMode.RESULTS -> ResultsView(
                    instanceUrl = instanceUrl,
                    query = currentMode.query,
                    isDark = isDark,
                    onClose = { finish() }
                )
            }
        }
    }
}

private sealed class SearchMode {
    object INPUT : SearchMode()
    data class RESULTS(val query: String) : SearchMode()
}

@Composable
fun SearchOverlay(
    instanceUrl: String,
    isDark: Boolean,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var query by remember { mutableStateOf("") }

    val cardBg = if (isDark) Color(0xFF1C1B1F) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color.White else Color(0xFF1C1B1F)
    val placeholderColor = if (isDark) Color(0xFF9E9E9E) else Color(0xFF757575)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = cardBg
                )
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = {
                        Text(
                            "Search with SearXNG\u2026",
                            color = placeholderColor,
                            fontSize = 16.sp
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = textColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .padding(4.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (query.isNotBlank()) {
                                onSearch(query.trim())
                            }
                        }
                    ),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        cursorColor = textColor
                    )
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
fun ResultsView(
    instanceUrl: String,
    query: String,
    isDark: Boolean,
    onClose: () -> Unit
) {
    val searchUrl = "$instanceUrl/search?q=${Uri.encode(query)}"
    var canGoBack by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    val bgColor = if (isDark) Color(0xFF1C1B1F) else Color(0xFFFAFAFA)
    val webViewBg = if (isDark) android.graphics.Color.BLACK else android.graphics.Color.WHITE

    BackHandler(enabled = true) {
        if (canGoBack) {
            webView?.goBack()
        } else {
            onClose()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .background(bgColor)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                canGoBack = view?.canGoBack() ?: false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                canGoBack = view?.canGoBack() ?: false
                                isLoading = false
                                if (isDark) {
                                    view?.evaluateJavascript("""
(function(){
if(document.getElementById('__se_dark'))return;
var bg=getComputedStyle(document.body).backgroundColor;
var m=bg.match(/\d+/g);
if(!m)return;
var l=parseInt(m[0])*299+parseInt(m[1])*587+parseInt(m[2])*114;
if(l>150000){
var s=document.createElement('style');
s.id='__se_dark';
s.textContent=[
'html{background:#121212!important}',
'body{background:#121212!important;color:#e0e0e0!important}',
'.result,.result-default{background:#1e1e1e!important;border-color:#333!important}',
'.result a,.result h3 a{color:#8ab4f8!important}',
'.result .content{color:#c0c0c0!important}',
'.url_wrapper,.url_i1,.url_o1{color:#7aa2f7!important}',
'#search_header{background:#1e1e1e!important}',
'.category_button{color:#e0e0e0!important}',
'.category_button.selected{background:#333!important}',
'input#q{background:#2a2a2a!important;color:#e0e0e0!important;border-color:#444!important}',
'.search_filters select{background:#2a2a2a!important;color:#e0e0e0!important;border-color:#444!important}',
'footer{background:#121212!important}',
'.sidebar-collapsible{background:#1e1e1e!important;color:#e0e0e0!important}',
'#links_on_top a{color:#8ab4f8!important}',
'.suggestion{color:#8ab4f8!important}',
'.highlight{color:#ffd54f!important}',
'.engines span{color:#888!important}',
'.engine-stats{color:#c0c0c0!important}',
'.selectable_url pre{background:#2a2a2a!important;color:#c0c0c0!important}',
'.cache_link{color:#7aa2f7!important}'
].join('');
document.head.appendChild(s);
}
})();
""".trimIndent(), null)
                                }
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress
                            }
                        }
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        setBackgroundColor(webViewBg)
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
                    color = Color(0xFF0057B7),
                    trackColor = Color.Transparent,
                )
            }
        }
    }
}
