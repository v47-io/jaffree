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
package io.v47.jaffree.ffmpeg

import com.github.kokorin.jaffree.JaffreeException
import com.github.kokorin.jaffree.LogLevel
import com.github.kokorin.jaffree.ffmpeg.FFmpeg
import com.github.kokorin.jaffree.ffmpeg.FFmpegResult
import com.github.kokorin.jaffree.ffmpeg.OutputListener
import com.github.kokorin.jaffree.util.ParseUtil
import io.v47.jaffree.process.LinesProcessHandler
import io.v47.jaffree.process.ProcessAccess
import io.v47.jaffree.process.ProcessAccessor
import io.v47.jaffree.utils.parseLogMessage
import org.slf4j.LoggerFactory


private val ffmpegLogger = LoggerFactory.getLogger(FFmpeg::class.java)!!

internal class FFmpegProcessHandler(
    private val outputListener: OutputListener?
) : LinesProcessHandler<FFmpegResult>(), ProcessAccessor {
    private lateinit var processAccess: ProcessAccess

    override fun setProcessAccess(processAccess: ProcessAccess) {
        this.processAccess = processAccess
    }

    override fun onStderrLine(line: String) {
        if ("frame=" in line && "bitrate=" in line && "speed=" in line)
            return

        val possibleResult = ParseUtil.parseResult(line)
        if (possibleResult != null) {
            result = possibleResult
            finalErrorMessage = null
            return
        }

        onStdoutLine(line)
    }

    override fun onStdoutLine(line: String) {
        val logMessage = parseLogMessage(line)
        if (logMessage != null) {
            processLastLogMessage(ffmpegLogger, ::handleLogMessage)

            startLogMessage(logMessage)
        } else
            appendOrLogLine(ffmpegLogger, line.trim())
    }

    override fun onExit() {
        processLastLogMessage(ffmpegLogger, ::handleLogMessage)

        if (finalErrorMessage != null)
            exception = JaffreeException(finalErrorMessage)
        else if (result == null)
            result = FFmpegResult(null, null, null, null, null, null)
    }

    private fun handleLogMessage(logLevel: LogLevel, message: String) {
        if (logLevel.isInfoOrHigher)
            runCatching {
                outputListener?.onOutput(message, processAccess)
            }.onFailure { x ->
                ffmpegLogger.warn("Exception in output listener", x)
            }

        val possibleResult = ParseUtil.parseResult(message)
        if (possibleResult != null) {
            result = possibleResult
            finalErrorMessage = null
        }
    }
}
