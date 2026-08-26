package com.zionhuang.music.lyrics

import android.text.format.DateUtils
import com.zionhuang.music.ui.component.animateScrollDuration

@Suppress("RegExpRedundantEscape")
object LyricsUtils {
    val LINE_REGEX = "((\\[\\d{1,2}:\\d{2}(?:\\.\\d{1,3})?\\])+)(.*)".toRegex()
    val TIME_REGEX = "\\[(\\d{1,2}):(\\d{2})(?:\\.(\\d{1,3}))?\\]".toRegex()

    fun parseLyrics(lyrics: String): List<LyricsEntry> =
        lyrics.lines()
            .flatMap { line ->
                parseLine(line).orEmpty()
            }.sorted()

    private fun parseLine(line: String): List<LyricsEntry>? {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return null
        val matchResult = LINE_REGEX.matchEntire(trimmed) ?: return null
        val times = matchResult.groupValues[1]
        val text = matchResult.groupValues[3]
        val timeMatchResults = TIME_REGEX.findAll(times)

        val entries = timeMatchResults.map { timeMatchResult ->
            val min = timeMatchResult.groupValues[1].toLongOrNull() ?: 0L
            val sec = timeMatchResult.groupValues[2].toLongOrNull() ?: 0L
            val milString = timeMatchResult.groupValues.getOrNull(3).orEmpty()
            var mil = milString.toLongOrNull() ?: 0L
            if (milString.length == 1) {
                mil *= 100
            } else if (milString.length == 2) {
                mil *= 10
            }
            val time = min * DateUtils.MINUTE_IN_MILLIS + sec * DateUtils.SECOND_IN_MILLIS + mil
            LyricsEntry(time, text)
        }.toList()
        return entries.ifEmpty { null }
    }

    fun findCurrentLineIndex(lines: List<LyricsEntry>, position: Long): Int {
        if (lines.isEmpty()) return -1
        for (index in lines.indices) {
            if (lines[index].time > position + animateScrollDuration) {
                return (index - 1).coerceAtLeast(0)
            }
        }
        return lines.lastIndex
    }
}