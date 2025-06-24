/**
 * Copyright (C) 2025 jaffree Authors
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

import org.slf4j.LoggerFactory

/**
 * Grants access to the exact point when a process has started or stopped.
 *
 * This can be used to get access to the [ProcessAccess] before it can be
 * obtained from the [ProcessFuture].
 *
 * @author Inspired by @Speiger
 */
interface ProcessListener {
    /**
     * Called immediately after the process started.
     *
     * This method must not fail (i.e. throw an exception).
     */
    fun onStart(processAccess: ProcessAccess)

    /**
     * Called immediately after the process stopped.
     *
     * This method must not fail (i.e. throw an exception).
     */
    fun onStop(processAccess: ProcessAccess, exitCode: Int)
}

internal fun ProcessListener.onStartSafe(processAccess: ProcessAccess) {
    runCatching {
        onStart(processAccess)
    }.onFailure { x ->
        LoggerFactory.getLogger(javaClass).debug("onStart failed", x)
    }
}

internal fun ProcessListener.onStopSafe(processAccess: ProcessAccess, exitCode: Int) {
    runCatching {
        onStop(processAccess, exitCode)
    }.onFailure { x ->
        LoggerFactory.getLogger(javaClass).debug("onStop failed", x)
    }
}
