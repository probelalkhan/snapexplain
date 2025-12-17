package dev.belalkhan.snapexplain.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    val annotatedString = buildAnnotatedString {
        parseMarkdown(markdown, color)
    }
    
    androidx.compose.foundation.text.selection.SelectionContainer {
        androidx.compose.material3.Text(
            text = annotatedString,
            modifier = modifier,
            style = style.copy(
                fontSize = 15.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.25.sp
            )
        )
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.parseMarkdown(
    text: String,
    baseColor: Color
) {
    var currentIndex = 0
    val lines = text.lines()
    
    lines.forEachIndexed { lineIndex, line ->
        when {
            // Code block (```)
            line.trim().startsWith("```") -> {
                // Skip the opening ```
                return@forEachIndexed
            }
            
            // Headers (#, ##, ###)
            line.startsWith("# ") -> {
                withStyle(SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = baseColor
                )) {
                    append(line.substring(2))
                }
                append("\n")
            }
            line.startsWith("## ") -> {
                withStyle(SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = baseColor
                )) {
                    append(line.substring(3))
                }
                append("\n")
            }
            line.startsWith("### ") -> {
                withStyle(SpanStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = baseColor
                )) {
                    append(line.substring(4))
                }
                append("\n")
            }
            
            // List items (-, *, 1.)
            line.trim().startsWith("- ") || line.trim().startsWith("* ") -> {
                append("  • ")
                parseInlineMarkdown(line.trim().substring(2), baseColor)
                append("\n")
            }
            line.trim().matches(Regex("^\\d+\\.\\s.*")) -> {
                val content = line.trim().substringAfter(". ")
                append("  ${line.trim().substringBefore(".")}. ")
                parseInlineMarkdown(content, baseColor)
                append("\n")
            }
            
            // Regular line
            else -> {
                parseInlineMarkdown(line, baseColor)
                if (lineIndex < lines.size - 1) append("\n")
            }
        }
    }
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.parseInlineMarkdown(
    text: String,
    baseColor: Color
) {
    var remaining = text
    
    while (remaining.isNotEmpty()) {
        when {
            // Bold (**text**)
            remaining.contains("**") -> {
                val start = remaining.indexOf("**")
                if (start > 0) {
                    append(remaining.substring(0, start))
                }
                remaining = remaining.substring(start + 2)
                
                val end = remaining.indexOf("**")
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = baseColor)) {
                        append(remaining.substring(0, end))
                    }
                    remaining = remaining.substring(end + 2)
                } else {
                    append("**$remaining")
                    remaining = ""
                }
            }
            
            // Italic (*text*)
            remaining.contains("*") && !remaining.contains("**") -> {
                val start = remaining.indexOf("*")
                if (start > 0) {
                    append(remaining.substring(0, start))
                }
                remaining = remaining.substring(start + 1)
                
                val end = remaining.indexOf("*")
                if (end != -1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = baseColor)) {
                        append(remaining.substring(0, end))
                    }
                    remaining = remaining.substring(end + 1)
                } else {
                    append("*$remaining")
                    remaining = ""
                }
            }
            
            // Inline code (`code`)
            remaining.contains("`") -> {
                val start = remaining.indexOf("`")
                if (start > 0) {
                    append(remaining.substring(0, start))
                }
                remaining = remaining.substring(start + 1)
                
                val end = remaining.indexOf("`")
                if (end != -1) {
                    withStyle(SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = Color(0xFF2B2B2B),
                        color = Color(0xFFE0E0E0)
                    )) {
                        append(" ${remaining.substring(0, end)} ")
                    }
                    remaining = remaining.substring(end + 1)
                } else {
                    append("`$remaining")
                    remaining = ""
                }
            }
            
            // Regular text
            else -> {
                append(remaining)
                remaining = ""
            }
        }
    }
}
