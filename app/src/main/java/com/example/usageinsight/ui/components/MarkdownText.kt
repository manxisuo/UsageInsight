package com.example.usageinsight.ui.components

import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        factory = { context ->
            TextView(context).apply {
                // 创建 Markwon 实例
                val markwon = Markwon.create(context)
                // 设置 Markdown 文本
                markwon.setMarkdown(this, markdown)
            }
        }
    )
} 