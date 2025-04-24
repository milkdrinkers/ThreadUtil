package io.github.milkdrinkers.threadutil.queue;

import io.github.milkdrinkers.threadutil.PlatformAdapter;
import io.github.milkdrinkers.threadutil.task.*;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a queue of tasks that can be executed with delays between tasks.
 *
 * @param <T> The type of data being passed through the queue
 */
public class TaskQueueFolia<T> extends TaskQueue<T> {
    public TaskQueueFolia(PlatformAdapter platform, Consumer<Throwable> errorHandler, Task<Void, T> initialTask) {
        super(platform, errorHandler, initialTask);
    }

    /**
     * Adds an asynchronous processing task to the queue.
     *
     * @param function The function to execute asynchronously
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public <R> TaskQueueFolia<R> async(Function<T, R> function) {
        return addTask(new AsyncTask<>(function));
    }

    /**
     * Adds an asynchronous processing task to the queue.
     *
     * @param consumer The consumer to execute asynchronously
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public TaskQueueFolia<Void> async(Consumer<T> consumer) {
        return async(convertToFunction(consumer));
    }

    /**
     * Adds an asynchronous processing task to the queue.
     *
     * @param callable The callable to execute asynchronously
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public <R> TaskQueueFolia<R> async(Callable<R> callable) {
        return async(convertToFunction(callable));
    }

    /**
     * Adds an asynchronous processing task to the queue.
     *
     * @param runnable The function to execute asynchronously
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public TaskQueueFolia<Void> async(Runnable runnable) {
        return async(Executors.callable(runnable, null));
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param function The function to execute on the main thread
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public <R> TaskQueueFolia<R> sync(Function<T, R> function) {
        return addTask(new SyncTask<>(function));
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param consumer The consumer to execute on the main thread
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public TaskQueueFolia<Void> sync(Consumer<T> consumer) {
        return sync(convertToFunction(consumer));
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param callable The callable to execute on the main thread
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public <R> TaskQueueFolia<R> sync(Callable<R> callable) {
        return sync(convertToFunction(callable));
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param runnable The function to execute on the main thread
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public TaskQueueFolia<Void> sync(Runnable runnable) {
        return sync(Executors.callable(runnable, null));
    }

    /**
     * Adds a delay before the next task in the queue.
     *
     * @param ticks The number of ticks to wait (20 ticks = 1 second)
     * @return The current {@link TaskQueueFolia} with delay added
     */
    public TaskQueueFolia<T> delay(long ticks) {
        return addTask(new DelayTask<>(ticks));
    }

    /**
     * Adds a delay before the next task in the queue.
     *
     * @param duration The duration to wait (converted to ticks)
     * @return The current {@link TaskQueueFolia} with delay added
     */
    public TaskQueueFolia<T> delay(Duration duration) {
        try {
            return delay(platform.toTicks(duration));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <R> TaskQueueFolia<R> addTask(Task<T, R> task) {
        this.tasks.add(task);
        return (TaskQueueFolia<R>) this;
    }

    // Start of folia specific methods

    // Location

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param location The location to run the task at
     * @param function The function to execute on the main thread
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public <R> TaskQueueFolia<R> sync(Location location, Function<T, R> function) {
        return addTask(new FoliaSyncTask<>(location, function));
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param location The location to run the task at
     * @param consumer The consumer to execute on the main thread
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public TaskQueueFolia<Void> sync(Location location, Consumer<T> consumer) {
        return sync(location, convertToFunction(consumer));
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param location The location to run the task at
     * @param callable The callable to execute on the main thread
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public <R> TaskQueueFolia<R> sync(Location location, Callable<R> callable) {
        return sync(location, convertToFunction(callable));
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param location The location to run the task at
     * @param runnable The function to execute on the main thread
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public TaskQueueFolia<Void> sync(Location location, Runnable runnable) {
        return sync(location, Executors.callable(runnable, null));
    }

    // Entity

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param entity   The entity to run the task at
     * @param function The function to execute on the main thread
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public <R> TaskQueueFolia<R> sync(Entity entity, Function<T, R> function) {
        return addTask(new FoliaSyncTask<>(entity, function));
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param entity   The entity to run the task at
     * @param consumer The consumer to execute on the main thread
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public TaskQueueFolia<Void> sync(Entity entity, Consumer<T> consumer) {
        return sync(entity, convertToFunction(consumer));
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param entity   The entity to run the task at
     * @param callable The callable to execute on the main thread
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public <R> TaskQueueFolia<R> sync(Entity entity, Callable<R> callable) {
        return sync(entity, convertToFunction(callable));
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param entity   The entity to run the task at
     * @param runnable The function to execute on the main thread
     * @return A new {@link TaskQueueFolia} with the added task
     */
    public TaskQueueFolia<Void> sync(Entity entity, Runnable runnable) {
        return sync(entity, Executors.callable(runnable, null));
    }

    // End of folia specific methods
}
