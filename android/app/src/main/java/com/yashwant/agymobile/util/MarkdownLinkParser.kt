package com.yashwant.agymobile.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.yashwant.agymobile.ui.theme.AntigravityBlue
import com.yashwant.agymobile.ui.theme.AntigravityCyan
import com.yashwant.agymobile.ui.theme.AntigravityDarkBg
import com.yashwant.agymobile.ui.theme.TextPrimary

object MarkdownLinkParser {

    private val MD_LINK_REGEX = Regex("\\[([^\\]]+)\\]\\(([^)]+)\\)")
    private val BARE_URL_REGEX = Regex("(https?://[^\\s<>\"]+|file://[^\\s<>\"]+|/(?:storage/emulated/0|sdcard|data/user/0)[^\\s<>\"]*)")
    private val BOLD_REGEX = Regex("\\*\\*([^*]+)\\*\\*")
    private val CODE_REGEX = Regex("`([^`]+)`")

    fun getIconForTarget(target: LinkTarget): String = target.icon

    fun parseAnnotatedMarkdown(
        rawText: String,
        onLinkClick: ((String) -> Unit)? = null
    ): AnnotatedString {
        return buildAnnotatedString {
            // First, find all markdown links and bare URLs
            val linkMatches = mutableListOf<LinkMatch>()

            for (m in MD_LINK_REGEX.findAll(rawText)) {
                val label = m.groupValues[1]
                val url = m.groupValues[2]
                val target = FileOpener.resolveTarget(url, label)
                linkMatches.add(
                    LinkMatch(
                        range = m.range,
                        displayLabel = "${target.icon}$label",
                        url = url
                    )
                )
            }

            // Bare URLs that don't overlap with any markdown link
            for (m in BARE_URL_REGEX.findAll(rawText)) {
                val overlaps = linkMatches.any { it.range.first <= m.range.first && m.range.last <= it.range.last }
                if (!overlaps) {
                    var rawVal = m.value
                    var endOffset = 0
                    while (rawVal.isNotEmpty() && (rawVal.endsWith(".") || rawVal.endsWith(",") || rawVal.endsWith(")") || rawVal.endsWith("`") || rawVal.endsWith("'") || rawVal.endsWith("\"") || rawVal.endsWith(";"))) {
                        rawVal = rawVal.dropLast(1)
                        endOffset++
                    }
                    if (rawVal.isNotEmpty()) {
                        val target = FileOpener.resolveTarget(rawVal)
                        val label = when (target) {
                            is LinkTarget.Directory -> "${target.icon}${target.file.name.ifBlank { rawVal }}"
                            is LinkTarget.Image -> "${target.icon}${target.file.name.ifBlank { rawVal }}"
                            is LinkTarget.Document -> "${target.icon}${target.file.name.ifBlank { rawVal }}"
                            is LinkTarget.Media -> "${target.icon}${target.file.name.ifBlank { rawVal }}"
                            is LinkTarget.Web, is LinkTarget.Unknown -> "${target.icon}$rawVal"
                        }
                        var matchStart = m.range.first
                        var matchEnd = m.range.last + 1 - endOffset
                        if (matchStart > 0 && rawText[matchStart - 1] == '`' && matchEnd < rawText.length && rawText[matchEnd] == '`') {
                            matchStart -= 1
                            matchEnd += 1
                        }
                        linkMatches.add(
                            LinkMatch(
                                range = matchStart until matchEnd,
                                displayLabel = label,
                                url = rawVal
                            )
                        )
                    }
                }
            }

            // Sort matches by start index
            linkMatches.sortBy { it.range.first }

            var cursor = 0
            for (link in linkMatches) {
                if (link.range.first > cursor) {
                    appendStyledSubtext(rawText.substring(cursor, link.range.first))
                }

                val linkStart = length
                val clickable = LinkAnnotation.Clickable(
                    tag = "URL",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = AntigravityBlue,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.SemiBold
                        )
                    ),
                    linkInteractionListener = {
                        onLinkClick?.invoke(link.url)
                    }
                )

                pushLink(clickable)
                pushStringAnnotation("URL", link.url)
                append(link.displayLabel)
                pop() // string annotation
                pop() // link annotation

                cursor = link.range.last + 1
            }

            if (cursor < rawText.length) {
                appendStyledSubtext(rawText.substring(cursor))
            }
        }
    }

    private fun AnnotatedString.Builder.appendStyledSubtext(subtext: String) {
        // Handle bold and inline code
        var cursor = 0
        val regex = Regex("(\\*\\*[^*]+\\*\\*|`[^`]+`)")
        val matches = regex.findAll(subtext)

        for (match in matches) {
            if (match.range.first > cursor) {
                append(subtext.substring(cursor, match.range.first))
            }

            val matched = match.value
            if (matched.startsWith("**") && matched.endsWith("**") && matched.length > 4) {
                val boldContent = matched.substring(2, matched.length - 2)
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = TextPrimary))
                append(boldContent)
                pop()
            } else if (matched.startsWith("`") && matched.endsWith("`") && matched.length > 2) {
                val codeContent = matched.substring(1, matched.length - 1)
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        color = AntigravityCyan,
                        background = AntigravityDarkBg
                    )
                )
                append(codeContent)
                pop()
            } else {
                append(matched)
            }

            cursor = match.range.last + 1
        }

        if (cursor < subtext.length) {
            append(subtext.substring(cursor))
        }
    }

    private data class LinkMatch(
        val range: IntRange,
        val displayLabel: String,
        val url: String
    )
}
