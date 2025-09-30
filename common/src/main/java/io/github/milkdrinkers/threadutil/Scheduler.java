package io.github.milkdrinkers.threadutil;

import io.github.milkdrinkers.threadutil.exception.SchedulerAlreadyShuttingDownException;
import io.github.milkdrinkers.threadutil.exception.SchedulerInitializationException;
import io.github.milkdrinkers.threadutil.exception.SchedulerNotInitializedException;
import io.github.milkdrinkers.threadutil.exception.SchedulerShutdownTimeoutException;
import io.github.milkdrinkers.threadutil.queue.TaskQueue;
import io.github.milkdrinkers.threadutil.task.*;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A fluent API for scheduling asynchronous and synchronous {@link Task}s sequentially,
 * error handling, cancellation support, and configurable delays.
 */
public class Scheduler {
    private static PlatformAdapter platform;
    private static volatile boolean isInitialized = false;
    private static volatile boolean isShuttingDown = false;
    private static volatile Consumer<Throwable> errorHandler = Throwable::printStackTrace;

    protected Scheduler() {
    }

    private static void setPlatform(PlatformAdapter platform) {
        Scheduler.platform = platform;
    }

    static PlatformAdapter getPlatform() {
        return platform;
    }

    private static void setInitialized(boolean isInitialized) {
        Scheduler.isInitialized = isInitialized;
    }

    private static void setShuttingDown(boolean isShuttingDown) {
        Scheduler.isShuttingDown = isShuttingDown;
    }

    /**
     * Checks if the scheduler is in the process of shutting down.
     *
     * @return boolean
     */
    public static boolean isShuttingDown() {
        return isShuttingDown;
    }

    /**
     * Checks if the scheduler has been initialized. This returns true when the schedule is ready for usage.
     *
     * @return boolean
     */
    public static boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Initializes the scheduler with required platform.
     *
     * @param platform The platform instance
     * @throws SchedulerInitializationException if the scheduler is already initialized
     * @see PlatformAdapter
     */
    public static void init(PlatformAdapter platform) throws SchedulerInitializationException {
        if (isInitialized())
            throw new SchedulerInitializationException("Scheduler is already initialized");

        if (isShuttingDown())
            throw new SchedulerInitializationException("Scheduler is in the process of shutting down");

        if (platform == null)
            throw new SchedulerInitializationException("Platform must not be null");

        setPlatform(platform);
        setInitialized(true);
    }

    /**
     * Shuts down the scheduler and cancels all running task queues. Defaults to a 60 second timeout on running task queues.
     *
     * @throws SchedulerNotInitializedException      if the scheduler is not initialized
     * @throws SchedulerAlreadyShuttingDownException if the scheduler is already shutting down
     * @throws SchedulerShutdownTimeoutException     thrown if the scheduler is not shut down in time
     */
    public static void shutdown() throws SchedulerNotInitializedException, SchedulerAlreadyShuttingDownException, SchedulerShutdownTimeoutException {
        shutdown(Duration.ofSeconds(60L));
    }

    /**
     * Shuts down the scheduler and cancels all running task queues.
     *
     * @param duration The duration to wait before killing any incomplete task queues.
     * @throws SchedulerNotInitializedException      if the scheduler is not initialized
     * @throws SchedulerAlreadyShuttingDownException if the scheduler is already shutting down
     * @throws SchedulerShutdownTimeoutException     thrown if the scheduler is not shut down in time
     */
    public static void shutdown(Duration duration) throws SchedulerNotInitializedException, SchedulerAlreadyShuttingDownException, SchedulerShutdownTimeoutException {
        if (!isInitialized())
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        if (isShuttingDown())
            throw new SchedulerAlreadyShuttingDownException("Scheduler is already shutting down");

        setShuttingDown(true);
        platform.shutdown(duration);
        setPlatform(null);
        setInitialized(false);
        setShuttingDown(false);
    }

    /**
     * Sets a global error handler for all task queues.
     *
     * @param handler The consumer that will receive thrown exceptions
     */
    public static void setErrorHandler(Consumer<Throwable> handler) {
        errorHandler = handler;
    }

    static Consumer<Throwable> getErrorHandler() {
        return errorHandler;
    }

    /**
     * Starts a new asynchronous task queue.
     * Supports regular values and CompletableFuture return types.
     *
     * @param function The operation to execute asynchronously
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueue} instance
     */
    public static <R> TaskQueue<R> async(Function<Void, R> function) {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueue<>(getPlatform(), getErrorHandler(), new AsyncTask<>(function));
    }

    /**
     * Starts a new asynchronous task queue with access to task context.
     * Supports regular values and CompletableFuture return types.
     *
     * @param function The operation to execute asynchronously
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueue} instance
     */
    public static <R> TaskQueue<R> async(java.util.function.BiFunction<Void, TaskContext, R> function) {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueue<>(getPlatform(), getErrorHandler(),
            new AsyncTask<>(input -> function.apply(input, new TaskContext(new java.util.concurrent.atomic.AtomicBoolean(false)))));
    }

    /**
     * Starts a new asynchronous task queue.
     *
     * @param callable The operation to execute asynchronously
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueue} instance
     */
    public static <R> TaskQueue<R> async(Callable<R> callable) {
        return Scheduler.async(convertToFunction(callable));
    }

    /**
     * Starts a new asynchronous task queue.
     *
     * @param runnable The operation to execute asynchronously
     * @return A new {@link TaskQueue} instance
     */
    public static TaskQueue<Void> async(Runnable runnable) {
        return Scheduler.async(Executors.callable(runnable, null));
    }

    /**
     * Starts a new asynchronous CompletableFuture task queue.
     *
     * @param future The CompletableFuture to execute
     * @param <R>    The return type of the initial task
     * @return A new {@link TaskQueue} instance
     */
    public static <R> TaskQueue<R> async(CompletableFuture<R> future) {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueue<>(getPlatform(), getErrorHandler(), new FutureTask<>(_ignored -> future, false));
    }

    // ========== SYNC METHODS ==========

    /**
     * Starts a new synchronous task queue.
     * Supports regular values and CompletableFuture return types.
     *
     * @param function The operation to execute on the main thread
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueue} instance
     */
    public static <R> TaskQueue<R> sync(Function<Void, R> function) {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueue<>(getPlatform(), getErrorHandler(), new SyncTask<>(function));
    }

    /**
     * Starts a new synchronous task queue with access to task context.
     * Supports regular values and CompletableFuture return types.
     *
     * @param function The operation to execute on the main thread
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueue} instance
     */
    public static <R> TaskQueue<R> sync(java.util.function.BiFunction<Void, TaskContext, R> function) {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueue<>(getPlatform(), getErrorHandler(),
            new SyncTask<>(input -> function.apply(input, new TaskContext(new java.util.concurrent.atomic.AtomicBoolean(false)))));
    }

    /**
     * Starts a new synchronous task queue.
     *
     * @param callable The operation to execute on the main thread
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueue} instance
     */
    public static <R> TaskQueue<R> sync(Callable<R> callable) {
        return Scheduler.sync(convertToFunction(callable));
    }

    /**
     * Starts a new synchronous task queue.
     *
     * @param runnable The operation to execute on the main thread
     * @return A new {@link TaskQueue} instance
     */
    public static TaskQueue<Void> sync(Runnable runnable) {
        return Scheduler.sync(Executors.callable(runnable, null));
    }

    /**
     * Starts a new synchronous CompletableFuture task queue.
     *
     * @param future The CompletableFuture to execute
     * @param <R>    The return type of the initial task
     * @return A new {@link TaskQueue} instance
     */
    public static <R> TaskQueue<R> sync(CompletableFuture<R> future) {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueue<>(getPlatform(), getErrorHandler(), new FutureTask<>(_ignored -> future, true));
    }

    /**
     * Starts a new looping task queue that executes asynchronously at regular intervals.
     * The loop continues until TaskContext.cancel() is called.
     *
     * @param consumer   The bi-consumer to execute repeatedly
     * @param interval The number of ticks between iterations
     * @return A new {@link TaskQueue} instance
     */
    public static TaskQueue<Void> loopAsync(BiConsumer<Void, TaskContext> consumer, long interval) {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueue<>(getPlatform(), getErrorHandler(), new LoopTask<>(consumer, interval, false));
    }

    /**
     * Starts a new looping task queue that executes asynchronously at regular intervals.
     * The loop continues until TaskContext.cancel() is called.
     *
     * @param consumer The bi-consumer to execute repeatedly
     * @param duration The duration between iterations
     * @return A new {@link TaskQueue} instance
     */
    public static TaskQueue<Void> loopAsync(BiConsumer<Void, TaskContext> consumer, Duration duration) {
        return loopAsync(consumer, platform.toTicks(duration));
    }

    /**
     * Starts a new looping task queue that executes synchronously at regular intervals.
     * The loop continues until TaskContext.cancel() is called.
     *
     * @param consumer   The bi-consumer to execute repeatedly
     * @param interval The number of ticks between iterations
     * @return A new {@link TaskQueue} instance
     */
    public static TaskQueue<Void> loopSync(BiConsumer<Void, TaskContext> consumer, long interval) {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueue<>(getPlatform(), getErrorHandler(), new LoopTask<>(consumer, interval, true));
    }

    /**
     * Starts a new looping task queue that executes synchronously at regular intervals.
     * The loop continues until TaskContext.cancel() is called.
     *
     * @param consumer The bi-consumer to execute repeatedly
     * @param duration The duration between iterations
     * @return A new {@link TaskQueue} instance
     */
    public static TaskQueue<Void> loopSync(BiConsumer<Void, TaskContext> consumer, Duration duration) {
        return loopSync(consumer, platform.toTicks(duration));
    }

    /**
     * Starts a new delayed task queue.
     *
     * @param ticks The number of ticks to wait (20 ticks = 1 second)
     * @return The current {@link TaskQueue} with delay added
     */
    public static TaskQueue<Void> delay(long ticks) {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueue<>(getPlatform(), getErrorHandler(), new DelayTask<>(ticks));
    }

    /**
     * Starts a new delayed task queue.
     *
     * @param duration The duration to wait (converted to ticks)
     * @return The current {@link TaskQueue} with delay added
     */
    public static TaskQueue<Void> delay(Duration duration) {
        return delay(platform.toTicks(duration));
    }

    /**
     * Internal utility method to convert a {@link Callable} to a {@link Function}.
     *
     * @param callable callable
     * @param <R>      the return type of the callable
     * @return function
     */
    @ApiStatus.Internal
    static <R> Function<Void, R> convertToFunction(Callable<R> callable) {
        return (_ignored) -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}