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

import com.github.kokorin.jaffree.process.JaffreeAbnormalExitException
import com.github.kokorin.jaffree.process.ProcessHelper
import com.zaxxer.nuprocess.NuProcessBuilder
import io.v47.jaffree.utils.generateRandomId
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private val logger = LoggerFactory.getLogger(ProcessRunner::class.java)!!

internal class ProcessRunner<T>(
    private val executable: Path,
    private val arguments: List<String>,
    private val helpers: List<Runnable>,
    private val processHandler: JaffreeProcessHandler<T>
) {
    @Suppress("LongMethod", "ThrowsCount")
    @Synchronized
    fun executeAsync(): ProcessFuture<T> {
        val execTag = generateRandomId()

        val command =
            buildList {
                add("$executable")
                addAll(arguments)
            }

        logger.info(
            "[{}] Command constructed:\n{}",
            execTag,
            command.joinToString(" ") { if (' ' in it) "\"$it\"" else it }
        )

        val processAccess = ProcessAccessImpl(command.joinToString(separator = " "))

        (processHandler as? ProcessAccessor)?.setProcessAccess(processAccess)

        helpers
            .asSequence()
            .filterIsInstance<ProcessAccessor>()
            .forEach {
                it.setProcessAccess(processAccess)
            }

        val threadPool =
            if (helpers.isNotEmpty())
                Executors.newFixedThreadPool(helpers.size)
            else
                null

        return ProcessFuture(
            CompletableFuture.supplyAsync {
                val helperFutures = helpers.map { threadPool!!.submit(it) }

                logger.info("[{}] Starting process: {}", execTag, executable)

                val actualProcessHandler =
                    DelegatingProcessHandler(processHandler, processAccess)

                val nuProcessBuilder = NuProcessBuilder(actualProcessHandler, command)
                nuProcessBuilder.environment()["AV_LOG_FORCE_NOCOLOR"] = "1"

                val process = nuProcessBuilder.start()

                logger.debug("[{}] Waiting for process to finish", execTag)
                val status = process.waitFor(0, TimeUnit.SECONDS)

                logger.info("[{}] Process finished with status: {}", execTag, status)

                helpers.forEach {
                    (it as? ProcessHelper)?.close()
                }

                helperFutures.forEach {
                    it.get()
                }

                if (status != 0)
                    throw JaffreeAbnormalExitException(
                        errorExceptionMessage(status),
                        processHandler.errorLogMessages
                    ).also {
                        processHandler.exception?.let { x -> it.initCause(x) }
                    }

                processHandler.result
                    ?: throw NullPointerException("The result must not be null")
            }.whenComplete { result, x ->
                if (x != null)
                    logger.trace("[$execTag] Exception occurred", x)

                if (result != null)
                    logger.trace("[{}] Process produced result {}", execTag, result)

                threadPool?.shutdown()
            },
            processAccess
        )
    }

    private fun errorExceptionMessage(status: Int) =
        "Process execution has ended with non-zero status: $status. " +
                "Check logs for detailed error message."
}
