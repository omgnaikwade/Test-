package com.quest.cbt.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.quest.cbt.ui.theme.TextPrimary
import java.io.File

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun KatexMathView(
    text: String,
    modifier: Modifier = Modifier,
    baseDir: String? = null,
    images: List<String> = emptyList(),
    fontSizeSp: Int = 15
) {
    // Transform relative images into file:/// URIs if baseDir exists
    val processedHtml = remember(text, baseDir, images) {
        var result = text

        // Convert images array into <img> tags if not already embedded
        val imgTags = StringBuilder()
        images.forEach { imgPath ->
            val resolvedUri = when {
                imgPath.startsWith("http://") || imgPath.startsWith("https://") -> imgPath
                imgPath.startsWith("file://") -> imgPath
                baseDir != null -> {
                    val file = File(baseDir, imgPath.removePrefix("images/"))
                    if (file.exists()) "file://${file.absolutePath}" else "file://$baseDir/$imgPath"
                }
                else -> imgPath
            }
            imgTags.append("<img src=\"$resolvedUri\" alt=\"Question diagram\" />")
        }

        // Format LaTeX linebreaks and math escape
        val cleanText = result
            .replace("\\", "\\\\")
            .replace("`", "\\`")
            .replace("\$", "\\$")
            .replace("\n", "<br>")

        cleanText + imgTags.toString()
    }

    Box(modifier = modifier.fillMaxWidth()) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { context ->
                WebView(context).apply {
                    setBackgroundColor(AndroidColor.TRANSPARENT)
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        allowFileAccessFromFileURLs = true
                        allowUniversalAccessFromFileURLs = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                        defaultFontSize = fontSizeSp
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            // Inject content
                            val js = "setContent(`$processedHtml`);"
                            view?.evaluateJavascript(js, null)
                        }
                    }

                    loadUrl("file:///android_asset/katex/katex_template.html")
                }
            },
            update = { webView ->
                val js = "if (window.setContent) { setContent(`$processedHtml`); }"
                webView.evaluateJavascript(js, null)
            }
        )
    }
}
