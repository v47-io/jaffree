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

import com.github.kokorin.jaffree.JaffreeException
import com.github.kokorin.jaffree.process.JaffreeAbnormalExitException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executor
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

interface ProcessFuture<T> : Future<T>, CompletionStage<T> {
    companion object {
        operator fun <T> invoke(
            delegate: CompletableFuture<T>,
            processAccess: ProcessAccess
        ): ProcessFuture<T> =
            ProcessFutureImpl(delegate, processAccess)
    }

    val processAccess: ProcessAccess

    override fun get(): T

    override fun get(timeout: Long, unit: TimeUnit): T
}

@Suppress("TooManyFunctions")
private class ProcessFutureImpl<T>(
    private val delegate: CompletableFuture<T>,
    override val processAccess: ProcessAccess
) : ProcessFuture<T> {
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        if (mayInterruptIfRunning)
            processAccess.stopForcefully()
        else
            processAccess.stopGracefully()

        return delegate.cancel(mayInterruptIfRunning)
    }

    override fun isCancelled() = delegate.isCancelled

    override fun isDone() = delegate.isDone

    override fun get(): T =
        get(Long.MAX_VALUE, TimeUnit.MILLISECONDS)

    @Suppress("SwallowedException")
    override fun get(timeout: Long, unit: TimeUnit): T =
        try {
            delegate.get(timeout, unit)
        } catch (e: InterruptedException) {
            throw JaffreeException("Failed to execute, was interrupted", e)
        } catch (e: ExecutionException) {
            if (e.cause is JaffreeAbnormalExitException)
                throw e.cause!!
            else
                throw e
        }

    override fun <U> thenApply(fn: Function<in T, out U>): CompletionStage<U> =
        ProcessFutureImpl(delegate.thenApply(fn), processAccess)

    override fun <U> thenApplyAsync(fn: Function<in T, out U>): CompletionStage<U> =
        ProcessFutureImpl(delegate.thenApplyAsync(fn), processAccess)

    override fun <U> thenApplyAsync(
        fn: Function<in T, out U>,
        executor: Executor
    ): CompletionStage<U> =
        ProcessFutureImpl(delegate.thenApplyAsync(fn, executor), processAccess)

    override fun thenAccept(action: Consumer<in T>): CompletionStage<Void> =
        ProcessFutureImpl(delegate.thenAccept(action), processAccess)

    override fun thenAcceptAsync(action: Consumer<in T>): CompletionStage<Void> =
        ProcessFutureImpl(delegate.thenAcceptAsync(action), processAccess)

    override fun thenAcceptAsync(
        action: Consumer<in T>,
        executor: Executor
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.thenAcceptAsync(action, executor), processAccess)

    override fun thenRun(action: Runnable): CompletionStage<Void> =
        ProcessFutureImpl(delegate.thenRun(action), processAccess)

    override fun thenRunAsync(action: Runnable): CompletionStage<Void> =
        ProcessFutureImpl(delegate.thenRunAsync(action), processAccess)

    override fun thenRunAsync(action: Runnable, executor: Executor): CompletionStage<Void> =
        ProcessFutureImpl(delegate.thenRunAsync(action, executor), processAccess)

    override fun <U, V> thenCombine(
        other: CompletionStage<out U>,
        fn: BiFunction<in T, in U, out V>
    ): CompletionStage<V> =
        ProcessFutureImpl(delegate.thenCombine(other, fn), processAccess)

    override fun <U, V> thenCombineAsync(
        other: CompletionStage<out U>,
        fn: BiFunction<in T, in U, out V>
    ): CompletionStage<V> =
        ProcessFutureImpl(delegate.thenCombineAsync(other, fn), processAccess)

    override fun <U, V> thenCombineAsync(
        other: CompletionStage<out U>,
        fn: BiFunction<in T, in U, out V>,
        executor: Executor
    ): CompletionStage<V> =
        ProcessFutureImpl(delegate.thenCombineAsync(other, fn, executor), processAccess)

    override fun <U> thenAcceptBoth(
        other: CompletionStage<out U>,
        action: BiConsumer<in T, in U>
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.thenAcceptBoth(other, action), processAccess)

    override fun <U> thenAcceptBothAsync(
        other: CompletionStage<out U>,
        action: BiConsumer<in T, in U>
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.thenAcceptBothAsync(other, action), processAccess)

    override fun <U> thenAcceptBothAsync(
        other: CompletionStage<out U>,
        action: BiConsumer<in T, in U>,
        executor: Executor
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.thenAcceptBothAsync(other, action, executor), processAccess)

    override fun runAfterBoth(
        other: CompletionStage<*>,
        action: Runnable
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.runAfterBoth(other, action), processAccess)

    override fun runAfterBothAsync(
        other: CompletionStage<*>,
        action: Runnable
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.runAfterBothAsync(other, action), processAccess)

    override fun runAfterBothAsync(
        other: CompletionStage<*>,
        action: Runnable,
        executor: Executor
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.runAfterBothAsync(other, action, executor), processAccess)

    override fun <U> applyToEither(
        other: CompletionStage<out T>,
        fn: Function<in T, U>
    ): CompletionStage<U> =
        ProcessFutureImpl(delegate.applyToEither(other, fn), processAccess)

    override fun <U> applyToEitherAsync(
        other: CompletionStage<out T>,
        fn: Function<in T, U>
    ): CompletionStage<U> =
        ProcessFutureImpl(delegate.applyToEitherAsync(other, fn), processAccess)

    override fun <U> applyToEitherAsync(
        other: CompletionStage<out T>,
        fn: Function<in T, U>,
        executor: Executor
    ): CompletionStage<U> =
        ProcessFutureImpl(delegate.applyToEitherAsync(other, fn, executor), processAccess)

    override fun acceptEither(
        other: CompletionStage<out T>,
        action: Consumer<in T>
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.acceptEither(other, action), processAccess)

    override fun acceptEitherAsync(
        other: CompletionStage<out T>,
        action: Consumer<in T>
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.acceptEitherAsync(other, action), processAccess)

    override fun acceptEitherAsync(
        other: CompletionStage<out T>,
        action: Consumer<in T>,
        executor: Executor
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.acceptEitherAsync(other, action, executor), processAccess)

    override fun runAfterEither(
        other: CompletionStage<*>,
        action: Runnable
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.runAfterEither(other, action), processAccess)

    override fun runAfterEitherAsync(
        other: CompletionStage<*>,
        action: Runnable
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.runAfterEitherAsync(other, action), processAccess)

    override fun runAfterEitherAsync(
        other: CompletionStage<*>,
        action: Runnable,
        executor: Executor
    ): CompletionStage<Void> =
        ProcessFutureImpl(delegate.runAfterEitherAsync(other, action, executor), processAccess)

    override fun <U> thenCompose(fn: Function<in T, out CompletionStage<U>>): CompletionStage<U> =
        ProcessFutureImpl(delegate.thenCompose(fn), processAccess)

    override fun <U> thenComposeAsync(fn: Function<in T, out CompletionStage<U>>): CompletionStage<U> =
        ProcessFutureImpl(delegate.thenComposeAsync(fn), processAccess)

    override fun <U> thenComposeAsync(
        fn: Function<in T, out CompletionStage<U>>,
        executor: Executor
    ): CompletionStage<U> =
        ProcessFutureImpl(delegate.thenComposeAsync(fn, executor), processAccess)

    override fun <U> handle(fn: BiFunction<in T, Throwable, out U>): CompletionStage<U> =
        ProcessFutureImpl(delegate.handle(fn), processAccess)

    override fun <U> handleAsync(fn: BiFunction<in T, Throwable, out U>): CompletionStage<U> =
        ProcessFutureImpl(delegate.handleAsync(fn), processAccess)

    override fun <U> handleAsync(
        fn: BiFunction<in T, Throwable, out U>,
        executor: Executor
    ): CompletionStage<U> =
        ProcessFutureImpl(delegate.handleAsync(fn, executor), processAccess)

    override fun whenComplete(action: BiConsumer<in T, in Throwable>): CompletionStage<T> =
        ProcessFutureImpl(delegate.whenComplete(action), processAccess)

    override fun whenCompleteAsync(action: BiConsumer<in T, in Throwable>): CompletionStage<T> =
        ProcessFutureImpl(delegate.whenCompleteAsync(action), processAccess)

    override fun whenCompleteAsync(
        action: BiConsumer<in T, in Throwable>,
        executor: Executor
    ): CompletionStage<T> =
        ProcessFutureImpl(delegate.whenCompleteAsync(action, executor), processAccess)

    override fun exceptionally(fn: Function<Throwable, out T>): CompletionStage<T> =
        ProcessFutureImpl(delegate.exceptionally(fn), processAccess)

    override fun toCompletableFuture(): CompletableFuture<T> =
        delegate.toCompletableFuture()
}

