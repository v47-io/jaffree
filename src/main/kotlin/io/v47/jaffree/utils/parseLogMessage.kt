/**
 * Copyright (C) 2023 jaffree Authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package io.v47.jaffree.utils

import com.github.kokorin.jaffree.LogLevel
import com.github.kokorin.jaffree.log.LogMessage

private const val MAX_BRACKET_PAIRS_CHECKED = 2
private const val MAX_BRACKETS_DISTANCE = 8
private const val BRACKET_INDEX_LIMIT = 50

private val logLevels =
    mapOf(
        "info" to LogLevel.INFO,
        "verbose" to LogLevel.VERBOSE,
        "debug" to LogLevel.DEBUG,
        "warning" to LogLevel.WARNING,
        "error" to LogLevel.ERROR,
        "trace" to LogLevel.TRACE,
        "" to LogLevel.TRACE,
        "quiet" to LogLevel.QUIET,
        "panic" to LogLevel.PANIC,
        "fatal" to LogLevel.FATAL
    )

internal fun parseLogMessage(line: String): LogMessage? {
    val (logLevel, textRange) = findLogLevel(line) ?: return null

    val message = "${line.substring(0, textRange.first)}${line.substring(textRange.last + 1)}"

    return LogMessage(logLevel, message)
}

private fun findLogLevel(line: String): Pair<LogLevel, IntRange>? {
    var bracketPairsChecked = 0
    var lastClosingBracketIndex = 0

    while (bracketPairsChecked < MAX_BRACKET_PAIRS_CHECKED) {
        val openingBracketIndex = line.indexOf('[', lastClosingBracketIndex)
        if (openingBracketIndex < 0 || openingBracketIndex > BRACKET_INDEX_LIMIT)
            break

        lastClosingBracketIndex = line.indexOf(']', openingBracketIndex)
        if (lastClosingBracketIndex < 0 || lastClosingBracketIndex > BRACKET_INDEX_LIMIT)
            break

        val bracketsDistance = lastClosingBracketIndex - openingBracketIndex
        if (bracketsDistance <= MAX_BRACKETS_DISTANCE) {
            val logLevel =
                logLevels[line.subSequence(openingBracketIndex + 1, lastClosingBracketIndex)]

            if (logLevel != null)
                return logLevel to openingBracketIndex..lastClosingBracketIndex
        }

        bracketPairsChecked++
    }

    return null
}
