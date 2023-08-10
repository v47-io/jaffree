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
import com.zaxxer.nuprocess.NuProcessHandler
import java.nio.ByteBuffer

internal open class DefaultProcessHandler : NuProcessHandler {
    override fun onPreStart(nuProcess: NuProcess) = Unit
    override fun onStart(nuProcess: NuProcess) = Unit
    override fun onExit(exitCode: Int) = Unit
    override fun onStdout(buffer: ByteBuffer, closed: Boolean) = Unit
    override fun onStderr(buffer: ByteBuffer, closed: Boolean) = Unit
    override fun onStdinReady(buffer: ByteBuffer): Boolean = false
}
