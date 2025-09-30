package io.github.milkdrinkers.threadutil.queue;

import io.github.milkdrinkers.threadutil.PlatformAdapter;
import io.github.milkdrinkers.threadutil.TaskContext;
import io.github.milkdrinkers.threadutil.task.*;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a queue of tasks that can be executed with delays between tasks.
 *
 * @param <T> The type of data being passed through the queue
 */
public class TaskQueue<T> {
    protected final ConcurrentLinkedQueue<Task<?, ?>> tasks = new ConcurrentLinkedQueue<>();
    protected long currentTaskId = 0;
    protected final AtomicBoolean isCancelledFlag = new AtomicBoolean(false);

    protected final PlatformAdapter platform;
    protected final Consumer<Throwable> errorHandler;

    public TaskQueue(PlatformAdapter platform, Consumer<Throwable> errorHandler, Task<Void, T> initialTask) {
        this.platform = platform;
        this.errorHandler = errorHandler;
        tasks.add(initialTask);
    }

    /**
     * Adds an asynchronous processing task to the queue.
     * Supports regular values and CompletableFuture return types.
     *
     * @param function The function to execute asynchronously
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueue} with the added task
     */
    public <R> TaskQueue<R> async(Function<T, R> function) {
        return addTask(new AsyncTask<>(function));
    }

    /**
     * Adds an asynchronous processing task with access to task context.
     * Supports regular values and CompletableFuture return types.
     *
     * @param function The bi-function to execute asynchronously (receives input and TaskContext)
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueue} with the added task
     */
    public <R> TaskQueue<R> async(java.util.function.BiFunction<T, TaskContext, R> function) {
        return addTask(new AsyncTask<>(input -> function.apply(input, new TaskContext(isCancelledFlag))));
    }

    /**
     * Adds an asynchronous processing task to the queue.
     *
     * @param consumer The consumer to execute asynchronously
     * @return A new {@link TaskQueue} with the added task
     */
    public TaskQueue<Void> async(Consumer<T> consumer) {
        return async(convertToFunction(consumer));
    }

    /**
     * Adds an asynchronous processing task with access to task context.
     *
     * @param consumer The bi-consumer to execute asynchronously
     * @return A new {@link TaskQueue} with the added task
     */
    public TaskQueue<Void> async(BiConsumer<T, TaskContext> consumer) {
        return async((input, ctx) -> {
            consumer.accept(input, ctx);
            return null;
        });
    }

    /**
     * Adds an asynchronous processing task to the queue.
     *
     * @param callable The callable to execute asynchronously
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueue} with the added task
     */
    public <R> TaskQueue<R> async(Callable<R> callable) {
        return async(convertToFunction(callable));
    }

    /**
     * Adds an asynchronous processing task to the queue.
     *
     * @param runnable The function to execute asynchronously
     * @return A new {@link TaskQueue} with the added task
     */
    public TaskQueue<Void> async(Runnable runnable) {
        return async(Executors.callable(runnable, null));
    }

    /**
     * Adds an asynchronous CompletableFuture task to the queue.
     *
     * @param future The CompletableFuture to execute
     * @param <R>    The return type of this task
     * @return A new {@link TaskQueue} with the added task
     */
    public <R> TaskQueue<R> async(CompletableFuture<R> future) {
        return addTask(new FutureTask<>(convertToFunction(future), false));
    }

    /**
     * Adds a synchronous processing task to the queue.
     * Supports regular values and CompletableFuture return types.
     *
     * @param function The function to execute on the main thread
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueue} with the added task
     */
    public <R> TaskQueue<R> sync(Function<T, R> function) {
        return addTask(new SyncTask<>(function));
    }

    /**
     * Adds a synchronous processing task with access to task context.
     * Supports regular values and CompletableFuture return types.
     *
     * @param function The bi-function to execute on the main thread
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueue} with the added task
     */
    public <R> TaskQueue<R> sync(java.util.function.BiFunction<T, TaskContext, R> function) {
        return addTask(new SyncTask<>(input -> function.apply(input, new TaskContext(isCancelledFlag))));
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param consumer The consumer to execute on the main thread
     * @return A new {@link TaskQueue} with the added task
     */
    public TaskQueue<Void> sync(Consumer<T> consumer) {
        return sync(convertToFunction(consumer));
    }

    /**
     * Adds a synchronous processing task with access to task context.
     *
     * @param consumer The bi-consumer to execute on the main thread
     * @return A new {@link TaskQueue} with the added task
     */
    public TaskQueue<Void> sync(BiConsumer<T, TaskContext> consumer) {
        return sync((input, ctx) -> {
            consumer.accept(input, ctx);
            return null;
        });
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param callable The callable to execute on the main thread
     * @param <R>      The return type of this task
     * @return A new {@link TaskQueue} with the added task
     */
    public <R> TaskQueue<R> sync(Callable<R> callable) {
        return sync(convertToFunction(callable));
    }

    /**
     * Adds a synchronous processing task to the queue.
     *
     * @param runnable The function to execute on the main thread
     * @return A new {@link TaskQueue} with the added task
     */
    public TaskQueue<Void> sync(Runnable runnable) {
        return sync(Executors.callable(runnable, null));
    }

    /**
     * Adds a synchronous CompletableFuture task to the queue.
     *
     * @param future The CompletableFuture to execute
     * @param <R>    The return type of this task
     * @return A new {@link TaskQueue} with the added task
     */
    public <R> TaskQueue<R> sync(CompletableFuture<R> future) {
        return addTask(new FutureTask<>(convertToFunction(future), true));
    }

    /**
     * Adds a looping task that executes asynchronously at regular intervals.
     * The loop continues until the TaskContext.cancel() is called.
     *
     * @param consumer   The bi-consumer to execute repeatedly
     * @param interval The number of ticks between iterations
     * @return The current {@link TaskQueue} with the loop added
     */
    public TaskQueue<T> loopAsync(BiConsumer<T, TaskContext> consumer, long interval) {
        return addTask(new LoopTask<>(consumer, interval, false));
    }

    /**
     * Adds a looping task that executes asynchronously at regular intervals.
     * The loop continues until the TaskContext.cancel() is called.
     *
     * @param consumer The bi-consumer to execute repeatedly
     * @param duration The duration between iterations
     * @return The current {@link TaskQueue} with the loop added
     */
    public TaskQueue<T> loopAsync(BiConsumer<T, TaskContext> consumer, Duration duration) {
        return loopAsync(consumer, platform.toTicks(duration));
    }

    /**
     * Adds a looping task that executes synchronously at regular intervals.
     * The loop continues until the TaskContext.cancel() is called.
     *
     * @param consumer   The bi-consumer to execute repeatedly
     * @param interval The number of ticks between iterations
     * @return The current {@link TaskQueue} with the loop added
     */
    public TaskQueue<T> loopSync(BiConsumer<T, TaskContext> consumer, long interval) {
        return addTask(new LoopTask<>(consumer, interval, true));
    }

    /**
     * Adds a looping task that executes synchronously at regular intervals.
     * The loop continues until the TaskContext.cancel() is called.
     *
     * @param consumer The bi-consumer to execute repeatedly
     * @param duration The duration between iterations
     * @return The current {@link TaskQueue} with the loop added
     */
    public TaskQueue<T> loopSync(BiConsumer<T, TaskContext> consumer, Duration duration) {
        return loopSync(consumer, platform.toTicks(duration));
    }

    /**
     * Adds a delay before the next task in the queue.
     *
     * @param ticks The number of ticks to wait (20 ticks = 1 second)
     * @return The current {@link TaskQueue} with delay added
     */
    public TaskQueue<T> delay(long ticks) {
        return addTask(new DelayTask<>(ticks));
    }

    /**
     * Adds a delay before the next task in the queue.
     *
     * @param duration The duration to wait (converted to ticks)
     * @return The current {@link TaskQueue} with delay added
     */
    public TaskQueue<T> delay(Duration duration) {
        return delay(platform.toTicks(duration));
    }

    /**
     * Executes the task queue and returns a handle for cancellation.
     *
     * @return Cancellable handle to abort execution
     */
    public RunningTaskQueue execute() {
        executeTask(null, isCancelledFlag);
        return new RunningTaskQueue(isCancelledFlag);
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    protected <R> TaskQueue<R> addTask(Task<T, R> task) {
        this.tasks.add(task);
        return (TaskQueue<R>) this;
    }

    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    protected <I> void executeTask(I input, AtomicBoolean isCancelledFlag) {
        if (isCancelledFlag.get())
            return;

        if (tasks.isEmpty())
            return;

        currentTaskId++;
        final Task<I, ?> task = (Task<I, ?>) tasks.poll();

        task.execute(input, result -> {
            // Schedule next task
            if (!tasks.isEmpty()) {
                executeTask(result, isCancelledFlag);
            }
        }, isCancelledFlag, platform, errorHandler);
    }

    /**
     * Internal method to get the current task ID.
     *
     * @return task ID
     */
    @ApiStatus.Internal
    private long getCurrentTaskId() {
        return currentTaskId;
    }

    /**
     * Internal utility method to convert a {@link Consumer} to a {@link Function}.
     *
     * @param consumer consumer
     * @param <T>      the input type of the consumer
     * @return function
     */
    @ApiStatus.Internal
    protected static <T> Function<T, Void> convertToFunction(Consumer<T> consumer) {
        return (passed) -> {
            try {
                consumer.accept(passed);
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Internal utility method to convert a {@link Callable} to a {@link Function}.
     *
     * @param callable callable
     * @param <T>      the input type of the callable
     * @param <R>      the return type of the callable
     * @return function
     */
    @ApiStatus.Internal
    protected static <T, R> Function<T, R> convertToFunction(Callable<R> callable) {
        return (_ignored) -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Internal utility method to convert a {@link CompletableFuture} to a {@link Function}.
     *
     * @param future future
     * @param <T>      the input type of the future
     * @param <R>      the return type of the future
     * @return function
     */
    @ApiStatus.Internal
    protected static <T, R> Function<T, CompletableFuture<R>> convertToFunction(CompletableFuture<R> future) {
        return (_ignored) -> future;
    }
}