/*
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

import com.zaxxer.nuprocess.NuProcess
import java.nio.ByteBuffer

internal class DelegatingProcessHandler(
    private val delegate: JaffreeProcessHandler<*>,
    private val processAccess: ProcessAccessImpl
) : DefaultProcessHandler() {
    override fun onPreStart(nuProcess: NuProcess) {
        processAccess.process = nuProcess
    }

    override fun onExit(exitCode: Int) {
        processAccess.process = null
        delegate.onExit()
    }

    override fun onStdout(buffer: ByteBuffer, closed: Boolean) {
        delegate.onStdout(buffer, closed)
    }

    override fun onStderr(buffer: ByteBuffer, closed: Boolean) {
        delegate.onStderr(buffer, closed)
    }

    override fun onStdinReady(buffer: ByteBuffer): Boolean {
        // This should only be called after wantWrite was called in ProcessAccessImpl.
        // It sends the quit signal to FFmpeg to shut down gracefully
        buffer.put('q'.code.toByte())
        buffer.flip()

        // Nothing more to write
        return false
    }
}
