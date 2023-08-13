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

/**
 * Gives access to some process properties and functions.
 */
interface ProcessAccess {
    /**
     * Returns the full command line string used to start the process
     */
    val commandLine: String

    /**
     * The OS id of the process
     */
    val pid: Int

    /**
     * Forcefully stops the running process. This may lead to data loss!
     */
    fun stopForcefully()

    /**
     * Sends the quit signal to FFmpeg to let it perform proper clean-up operations after
     * properly stopping its internal processes.
     */
    fun stopGracefully()
}
