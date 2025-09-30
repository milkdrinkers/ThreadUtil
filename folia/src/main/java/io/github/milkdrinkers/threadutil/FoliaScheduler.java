package io.github.milkdrinkers.threadutil;

import io.github.milkdrinkers.threadutil.exception.SchedulerNotInitializedException;
import io.github.milkdrinkers.threadutil.queue.TaskQueueFolia;
import io.github.milkdrinkers.threadutil.task.AsyncTask;
import io.github.milkdrinkers.threadutil.task.DelayTask;
import io.github.milkdrinkers.threadutil.task.FoliaSyncTask;
import io.github.milkdrinkers.threadutil.task.SyncTask;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * A scheduler implementation specifically for Folia based software
 */
public class FoliaScheduler extends Scheduler {
    protected FoliaScheduler() {
        super();
    }

    /**
     * Starts a new asynchronous task queue.
     *
     * @param function The operation to execute asynchronously
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueueFolia} instance
     */
    public static <R> TaskQueueFolia<R> async(Function<Void, R> function) {
        if (!isInitialized())
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueueFolia<>(getPlatform(), getErrorHandler(), new AsyncTask<>(function));
    }

    /**
     * Starts a new asynchronous task queue.
     *
     * @param callable The operation to execute asynchronously
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueueFolia} instance
     */
    public static <R> TaskQueueFolia<R> async(Callable<R> callable) {
        return async(convertToFunction(callable));
    }

    /**
     * Starts a new asynchronous task queue.
     *
     * @param runnable The operation to execute asynchronously
     * @return A new {@link TaskQueueFolia} instance
     */
    public static TaskQueueFolia<Void> async(Runnable runnable) {
        return async(Executors.callable(runnable, null));
    }

    /**
     * Starts a new synchronous task queue.
     *
     * @param function The operation to execute on the main thread
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueueFolia} instance
     */
    public static <R> TaskQueueFolia<R> sync(Function<Void, R> function) {
        if (!Scheduler.isInitialized())
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueueFolia<>(getPlatform(), getErrorHandler(), new SyncTask<>(function));
    }

    /**
     * Starts a new synchronous task queue.
     *
     * @param callable The operation to execute on the main thread
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueueFolia} instance
     */
    public static <R> TaskQueueFolia<R> sync(Callable<R> callable) {
        return FoliaScheduler.sync(convertToFunction(callable));
    }

    /**
     * Starts a new synchronous task queue.
     *
     * @param runnable The operation to execute on the main thread
     * @return A new {@link TaskQueueFolia} instance
     */
    public static TaskQueueFolia<Void> sync(Runnable runnable) {
        return FoliaScheduler.sync(Executors.callable(runnable, null));
    }

    /**
     * Starts a new delayed task queue.
     *
     * @param ticks The number of ticks to wait (20 ticks = 1 second)
     * @return The current {@link TaskQueueFolia} with delay added
     */
    public static TaskQueueFolia<Void> delay(long ticks) {
        if (!isInitialized())
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueueFolia<>(getPlatform(), getErrorHandler(), new DelayTask<>(ticks));
    }

    /**
     * Starts a new delayed task queue.
     *
     * @param duration The duration to wait (converted to ticks)
     * @return The current {@link TaskQueueFolia} with delay added
     */
    public static TaskQueueFolia<Void> delay(Duration duration) {
        return delay(getPlatform().toTicks(duration));
    }

    // Folia specific methods

    // Location

    /**
     * Starts a new synchronous task queue.
     *
     * @param location The location to run the task at
     * @param function The operation to execute on the main thread
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueueFolia} instance
     */
    public static <R> TaskQueueFolia<R> sync(Location location, Function<Void, R> function) {
        if (!Scheduler.isInitialized())
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueueFolia<>(getPlatform(), getErrorHandler(), new FoliaSyncTask<>(location, function));
    }

    /**
     * Starts a new synchronous task queue.
     *
     * @param location The location to run the task at
     * @param callable The operation to execute on the main thread
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueueFolia} instance
     */
    public static <R> TaskQueueFolia<R> sync(Location location, Callable<R> callable) {
        return sync(location, convertToFunction(callable));
    }

    /**
     * Starts a new synchronous task queue.
     *
     * @param location The location to run the task at
     * @param runnable The operation to execute on the main thread
     * @return A new {@link TaskQueueFolia} instance
     */
    public static TaskQueueFolia<Void> sync(Location location, Runnable runnable) {
        return sync(location, Executors.callable(runnable, null));
    }

    // Entity

    /**
     * Starts a new synchronous task queue.
     *
     * @param entity   The entity to run the task at
     * @param function The operation to execute on the main thread
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueueFolia} instance
     */
    public static <R> TaskQueueFolia<R> sync(Entity entity, Function<Void, R> function) {
        if (!Scheduler.isInitialized())
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new TaskQueueFolia<>(getPlatform(), getErrorHandler(), new FoliaSyncTask<>(entity, function));
    }

    /**
     * Starts a new synchronous task queue.
     *
     * @param entity   The entity to run the task at
     * @param callable The operation to execute on the main thread
     * @param <R>      The return type of the initial task
     * @return A new {@link TaskQueueFolia} instance
     */
    public static <R> TaskQueueFolia<R> sync(Entity entity, Callable<R> callable) {
        return sync(entity, convertToFunction(callable));
    }

    /**
     * Starts a new synchronous task queue.
     *
     * @param entity   The entity to run the task at
     * @param runnable The operation to execute on the main thread
     * @return A new {@link TaskQueueFolia} instance
     */
    public static TaskQueueFolia<Void> sync(Entity entity, Runnable runnable) {
        return sync(entity, Executors.callable(runnable, null));
    }

    // End of Folia specific methods
}
