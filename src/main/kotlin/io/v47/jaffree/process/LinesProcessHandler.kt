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
package io.v47.jaffree.process

import com.github.kokorin.jaffree.log.LogMessage
import java.nio.ByteBuffer
import kotlin.concurrent.Volatile

private const val CARRIAGE_RETURN = '\r'.code.toByte()
private const val NEWLINE = '\n'.code.toByte()

abstract class LinesProcessHandler<R> : JaffreeProcessHandler<R> {
    @Volatile
    override var result: R? = null
        protected set

    @Volatile
    override var exception: Exception? = null
        protected set(value) {
            if (field == null)
                field = value
            else
                value?.let { field!!.addSuppressed(it) }
        }

    override val errorLogMessages = mutableListOf<LogMessage>()

    private val currentStderrBytes = mutableListOf<ByteArray>()
    private val currentStdoutBytes = mutableListOf<ByteArray>()

    override fun onStderr(buffer: ByteBuffer, closed: Boolean) {
        while (buffer.hasRemaining()) {
            if (addToBytes(buffer, currentStderrBytes) && currentStderrBytes.isNotEmpty()) {
                onStderrLine(String(currentStderrBytes.join()))
                currentStderrBytes.clear()
            }
        }

        if (closed && currentStderrBytes.size > 0)
            onStderrLine(String(currentStderrBytes.join()))
    }

    override fun onStdout(buffer: ByteBuffer, closed: Boolean) {
        while (buffer.hasRemaining()) {
            if (addToBytes(buffer, currentStdoutBytes) && currentStdoutBytes.isNotEmpty()) {
                onStdoutLine(String(currentStdoutBytes.join()))
                currentStdoutBytes.clear()
            }
        }

        if (closed && currentStdoutBytes.isNotEmpty())
            onStdoutLine(String(currentStdoutBytes.join()))
    }

    protected fun addErrorLogMessage(message: LogMessage) {
        errorLogMessages.add(message)
    }

    abstract fun onStderrLine(line: String)
    abstract fun onStdoutLine(line: String)
}

private fun addToBytes(source: ByteBuffer, target: MutableList<ByteArray>): Boolean {
    var newLinePos = -1
    for (pos in source.position()..<source.limit()) {
        val byteAtPos = source[pos]
        if (byteAtPos == CARRIAGE_RETURN || byteAtPos == NEWLINE) {
            newLinePos = pos
            break
        }
    }

    val bytes =
        ByteArray(
            if (newLinePos > -1)
                newLinePos - source.position()
            else
                source.remaining()
        )

    source.get(bytes)
    target.add(bytes)

    if (newLinePos > -1) {
        // Consume line break characters and put cursor at the start of the next line in buffer
        for (pos in source.position() until source.limit()) {
            val byteAtPos = source.get()
            if (byteAtPos != CARRIAGE_RETURN && byteAtPos != NEWLINE) {
                // buffer position already advanced past first character in next
                // line so need to reset here
                source.position(pos)
                break
            }
        }
    }

    // indicates whether the bytes contained a line break
    return newLinePos > -1
}

private fun List<ByteArray>.join(): ByteArray {
    val result = ByteArray(sumOf { it.size })

    var bytesCopied = 0
    forEach { bytes ->
        System.arraycopy(bytes, 0, result, bytesCopied, bytes.size)
        bytesCopied += bytes.size
    }

    return result
}
