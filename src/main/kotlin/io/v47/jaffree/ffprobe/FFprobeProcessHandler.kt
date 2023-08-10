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
package io.v47.jaffree.ffprobe

import com.github.kokorin.jaffree.JaffreeException
import com.github.kokorin.jaffree.ffprobe.FFprobe
import com.github.kokorin.jaffree.ffprobe.FFprobeResult
import com.github.kokorin.jaffree.ffprobe.data.FormatParser
import io.v47.jaffree.process.LinesProcessHandler
import io.v47.jaffree.utils.parseLogMessage
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

private val ffprobeLogger = LoggerFactory.getLogger(FFprobe::class.java)!!

internal class FFprobeProcessHandler(
    private val parser: FormatParser
) : LinesProcessHandler<FFprobeResult>() {
    private var contentWritten = false
    private val stdOutputContent = ByteArrayOutputStream()
    private val stdOutputWriter = stdOutputContent.writer(Charsets.UTF_8)

    override fun onStderrLine(line: String) {
        val logMessage = parseLogMessage(line)
        if (logMessage != null) {
            processLastLogMessage(ffprobeLogger)

            startLogMessage(logMessage)
        } else
            appendOrLogLine(ffprobeLogger, line.trim())
    }

    override fun onStdoutLine(line: String) {
        if (line.isBlank())
            return

        if (contentWritten)
            stdOutputWriter.append('\n')

        stdOutputWriter.append(line)
        contentWritten = true
    }

    override fun onExit() {
        stdOutputWriter.close()
        stdOutputContent.close()

        processLastLogMessage(ffprobeLogger)

        if (finalErrorMessage != null) {
            exception = JaffreeException(finalErrorMessage)
            return
        }

        ByteArrayInputStream(stdOutputContent.toByteArray()).use { input ->
            runCatching {
                ffprobeLogger.debug("Reading probe data using {} parser", parser.formatName)

                parser.parse(input)
            }.onFailure { x ->
                exception = JaffreeException("Failed to parse probe data", x)
            }.onSuccess { probeData ->
                result = FFprobeResult(probeData)
            }
        }
    }
}
