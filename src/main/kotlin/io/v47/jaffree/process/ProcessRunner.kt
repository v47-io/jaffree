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
import java.util.concurrent.CompletionException
import java.util.concurrent.TimeUnit

private val logger = LoggerFactory.getLogger(ProcessRunner::class.java)!!

internal class ProcessRunner<T>(
    private val executable: Path,
    private val arguments: List<String>,
    private val helpers: List<Runnable>,
    private val processHandler: JaffreeProcessHandler<T>,
    private val processListener: ProcessListener?
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

        val processAccess = ProcessAccessImpl(command.joinToString(separator = " "), execTag)

        (processHandler as? ProcessAccessor)?.setProcessAccess(processAccess)

        helpers
            .asSequence()
            .filterIsInstance<ProcessAccessor>()
            .forEach {
                it.setProcessAccess(processAccess)
            }

        val threadPool =
            if (helpers.isNotEmpty())
                ProcessConfig.executorFactory(helpers.size)
            else
                null

        return ProcessFuture(
            CompletableFuture.supplyAsync {
                val helperFutures = helpers.map { threadPool!!.submit(it) }

                logger.info("[{}] Starting process: {}", execTag, executable)

                val actualProcessHandler =
                    DelegatingProcessHandler(
                        processHandler,
                        processAccess,
                        processListener
                    )

                val nuProcessBuilder = NuProcessBuilder(actualProcessHandler, command)
                nuProcessBuilder.environment()["AV_LOG_FORCE_NOCOLOR"] = "1"

                val exitCode: Int
                val result: Result<T>

                try {
                    val process = nuProcessBuilder.start()
                        ?: throw JaffreeAbnormalExitException(
                            "Process failed to start",
                            emptyList()
                        )

                    processAccess.process = process

                    logger.debug("[{}] Waiting for process to finish", execTag)
                    exitCode = process.waitFor(0, TimeUnit.SECONDS)
                    result = processHandler.getResult(exitCode)
                } finally {
                    processAccess.process = null

                    helpers.forEach {
                        (it as? ProcessHelper)?.close()
                    }

                    helperFutures.forEach {
                        it.get()
                    }
                }

                logger.info("[{}] Process finished with status: {}", execTag, exitCode)

                if (exitCode != 0)
                    throw JaffreeAbnormalExitException(
                        errorExceptionMessage(exitCode),
                        processHandler.errorLogMessages
                    ).also {
                        if (result.isFailure)
                            it.addSuppressed(result.exceptionOrNull()!!)
                    }

                result.getOrThrow()
            }.whenComplete { result, x ->
                if (x != null)
                    logger.trace(
                        "[$execTag] Exception occurred",
                        (x as? CompletionException)?.cause ?: x
                    )

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
