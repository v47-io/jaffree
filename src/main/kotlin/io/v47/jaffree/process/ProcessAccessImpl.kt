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
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(ProcessAccess::class.java)!!

internal class ProcessAccessImpl(
    override val commandLine: String,
    private val execTag: String
) : ProcessAccess {
    internal var process: NuProcess? = null

    override val pid: Int
        get() = process?.pid ?: error("[$execTag] process not set, can't retrieve PID")

    override fun stopForcefully() {
        val process = process

        if (process == null)
            logger.error("[{}] No process set, can't stop", execTag)
        else if (process.isRunning)
            process.destroy(true)
    }

    override fun stopGracefully() {
        val process = process

        if (process == null)
            logger.error("[{}] No process set, can't stop", execTag)
        else if (process.isRunning)
        // wantWrite will lead to onStdinReady of the DelegatingProcessHandler to be called.
        // Hopefully the implementation is sound and never calls this anywhere else.
            process.wantWrite()
    }
}
