package io.github.milkdrinkers.threadutil.queue;

import io.github.milkdrinkers.threadutil.PlatformAdapter;
import io.github.milkdrinkers.threadutil.task.FoliaSyncTask;
import io.github.milkdrinkers.threadutil.task.Task;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

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
        return (TaskQueueFolia<R>) addTask(new FoliaSyncTask<>(location, function));
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
        return (TaskQueueFolia<R>) addTask(new FoliaSyncTask<>(entity, function));
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
}
