/**
 * Copyright (C) 2024 jaffree Authors
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

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Creates the [ExecutorService] used to run various helper threads to facilitate
 * the proper execution of FFmpeg and FFprobe.
 */
typealias ExecutorFactory = (size: Int) -> ExecutorService

/**
 * Exposes global configuration options related to the actual execution of FFmpeg and
 * FFprobe processes.
 */
object ProcessConfig {
    /**
     * Specifies the global [ExecutorFactory]
     */
    var executorFactory: ExecutorFactory = { size -> Executors.newFixedThreadPool(size) }
}
