package io.github.milkdrinkers.threadutil;

import io.github.milkdrinkers.threadutil.exception.SchedulerInitializationException;
import io.github.milkdrinkers.threadutil.exception.SchedulerNotInitializedException;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A fluent API for scheduling asynchronous and synchronous {@link Stage}s sequentially,
 * error handling, cancellation support, and configurable delays.
 */
public final class Scheduler {
    private static PlatformAdapter platform;
    private static volatile boolean isInitialized = false;
    private static volatile Consumer<Throwable> errorHandler = Throwable::printStackTrace;

    private Scheduler() {}

    /**
     * Initializes the scheduler with required platform.
     *
     * @param platform The platform instance
     * @throws SchedulerInitializationException if the scheduler is already initialized
     */
    public static void init(PlatformAdapter platform) {
        if (isInitialized)
            throw new SchedulerInitializationException("Scheduler is already initialized");

        if (platform == null)
            throw new SchedulerInitializationException("Platform must not be null");

        Scheduler.platform = platform;
        isInitialized = true;
    }

    /**
     * Shuts down the scheduler and cancels all pending stages.
     *
     * @throws SchedulerNotInitializedException if the scheduler is not initialized
     */
    public static void shutdown() {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        platform.shutdown(Duration.ofSeconds(60L));
        isInitialized = false;
    }

    /**
     * Sets a global error handler for all stage queues.
     *
     * @param handler The consumer that will receive thrown exceptions
     */
    public static void setErrorHandler(Consumer<Throwable> handler) {
        errorHandler = handler;
    }

    /**
     * Starts a new asynchronous stage queue.
     *
     * @param function The operation to execute asynchronously
     * @param <R> The return type of the initial stage
     * @return A new {@link StageQueue} instance
     */
    public static <R> StageQueue<R> async(Function<Void, R> function) {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new StageQueue<>(new AsyncStage<>(function));
    }

    /**
     * Starts a new asynchronous stage queue.
     *
     * @param callable The operation to execute asynchronously
     * @param <R> The return type of the initial stage
     * @return A new {@link StageQueue} instance
     */
    public static <R> StageQueue<R> async(Callable<R> callable) {
        return Scheduler.async(convertToFunction(callable));
    }

    /**
     * Starts a new asynchronous stage queue.
     *
     * @param runnable The operation to execute asynchronously
     * @return A new {@link StageQueue} instance
     */
    public static StageQueue<Void> async(Runnable runnable) {
        return Scheduler.async(Executors.callable(runnable, null));
    }

    /**
     * Starts a new synchronous stage queue.
     *
     * @param function The operation to execute on the main thread
     * @param <R> The return type of the initial stage
     * @return A new {@link StageQueue} instance
     */
    public static <R> StageQueue<R> sync(Function<Void, R> function) {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new StageQueue<>(new SyncStage<>(function));
    }

    /**
     * Starts a new synchronous stage queue.
     *
     * @param callable The operation to execute on the main thread
     * @param <R> The return type of the initial stage
     * @return A new {@link StageQueue} instance
     */
    public static <R> StageQueue<R> sync(Callable<R> callable) {
        return Scheduler.sync(convertToFunction(callable));
    }

    /**
     * Starts a new synchronous stage queue.
     *
     * @param runnable The operation to execute on the main thread
     * @return A new {@link StageQueue} instance
     */
    public static StageQueue<Void> sync(Runnable runnable) {
        return Scheduler.sync(Executors.callable(runnable, null));
    }

    /**
     * Starts a new delayed stage queue.
     *
     * @param ticks The number of ticks to wait (20 ticks = 1 second)
     * @return The current {@link StageQueue} with delay added
     */
    public static StageQueue<Void> delay(long ticks) {
        if (!isInitialized)
            throw new SchedulerNotInitializedException("Scheduler is not initialized");

        return new StageQueue<>(new DelayStage<>(ticks));
    }

    /**
     * Starts a new delayed stage queue.
     *
     * @param duration The duration to wait (converted to ticks)
     * @return The current {@link StageQueue} with delay added
     */
    public static StageQueue<Void> delay(Duration duration) {
        try {
            return delay(platform.toTicks(duration));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Represents a queue of stages that can be executed with delays between stages.
     *
     * @param <T> The type of data being passed through the queue
     */
    public static final class StageQueue<T> {
        private final ConcurrentLinkedQueue<Stage<?, ?>> stages = new ConcurrentLinkedQueue<>();
        private long currentStageId = 0;
        private final AtomicBoolean isCancelledFlag = new AtomicBoolean(false);

        private StageQueue() {
        }

        private StageQueue(Stage<Void, T> initialStage) {
            stages.add(initialStage);
        }

        /**
         * Adds an asynchronous processing stage to the queue.
         *
         * @param function The function to execute asynchronously
         * @param <R> The return type of this stage
         * @return A new {@link StageQueue} with the added stage
         */
        public <R> StageQueue<R> async(Function<T, R> function) {
            return addStage(new AsyncStage<>(function));
        }

        /**
         * Adds an asynchronous processing stage to the queue.
         *
         * @param consumer The consumer to execute asynchronously
         * @return A new {@link StageQueue} with the added stage
         */
        public StageQueue<Void> async(Consumer<T> consumer) {
            return async(convertToFunction(consumer));
        }

        /**
         * Adds an asynchronous processing stage to the queue.
         *
         * @param callable The callable to execute asynchronously
         * @param <R> The return type of this stage
         * @return A new {@link StageQueue} with the added stage
         */
        public <R> StageQueue<R> async(Callable<R> callable) {
            return async(convertToFunction(callable));
        }

        /**
         * Adds an asynchronous processing stage to the queue.
         *
         * @param runnable The function to execute asynchronously
         * @return A new {@link StageQueue} with the added stage
         */
        public StageQueue<Void> async(Runnable runnable) {
            return async(Executors.callable(runnable, null));
        }

        /**
         * Adds a synchronous processing stage to the queue.
         *
         * @param function The function to execute on the main thread
         * @param <R> The return type of this stage
         * @return A new {@link StageQueue} with the added stage
         */
        public <R> StageQueue<R> sync(Function<T, R> function) {
            return addStage(new SyncStage<>(function));
        }

        /**
         * Adds a synchronous processing stage to the queue.
         *
         * @param consumer The consumer to execute on the main thread
         * @return A new {@link StageQueue} with the added stage
         */
        public StageQueue<Void> sync(Consumer<T> consumer) {
            return sync(convertToFunction(consumer));
        }

        /**
         * Adds a synchronous processing stage to the queue.
         *
         * @param callable The callable to execute on the main thread
         * @param <R> The return type of this stage
         * @return A new {@link StageQueue} with the added stage
         */
        public <R> StageQueue<R> sync(Callable<R> callable) {
            return sync(convertToFunction(callable));
        }

        /**
         * Adds a synchronous processing stage to the queue.
         *
         * @param runnable The function to execute on the main thread
         * @return A new {@link StageQueue} with the added stage
         */
        public StageQueue<Void> sync(Runnable runnable) {
            return sync(Executors.callable(runnable, null));
        }

        /**
         * Adds a delay before the next stage in the queue.
         *
         * @param ticks The number of ticks to wait (20 ticks = 1 second)
         * @return The current {@link StageQueue} with delay added
         */
        public StageQueue<T> delay(long ticks) {
            return addStage(new DelayStage<>(ticks));
        }

        /**
         * Adds a delay before the next stage in the queue.
         *
         * @param duration The duration to wait (converted to ticks)
         * @return The current {@link StageQueue} with delay added
         */
        public StageQueue<T> delay(Duration duration) {
            try {
                return delay(platform.toTicks(duration));
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Executes the stage queue and returns a handle for cancellation.
         *
         * @return Cancellable handle to abort execution
         */
        public Cancellable execute() {
            executeStage(null, isCancelledFlag);
            return new CancellableStageQueue(isCancelledFlag);
        }

        @SuppressWarnings("unchecked")
        @ApiStatus.Internal
        private <R> StageQueue<R> addStage(Stage<T, R> stage) {
            this.stages.add(stage);
            return (StageQueue<R>) this;
        }

        @SuppressWarnings("unchecked")
        @ApiStatus.Internal
        private <I> void executeStage(I input, AtomicBoolean isCancelledFlag) {
            if (isCancelledFlag.get())
                return;

            if (stages.isEmpty())
                return;

            currentStageId++;
            final Stage<I, ?> stage = (Stage<I, ?>) stages.poll();

            stage.execute(input, result -> {
                // Schedule next stage
                if (!stages.isEmpty()) {
                    executeStage(result, isCancelledFlag);
                }
            }, isCancelledFlag);
        }

        /**
         * Internal method to get the current stage ID.
         * @return stage ID
         */
        @ApiStatus.Internal
        private long getCurrentStageId() {
            return currentStageId;
        }

        /**
         * Internal utility method to convert a {@link Consumer} to a {@link Function}.
         * @param consumer consumer
         * @return function
         * @param <T> the input type of the consumer
         */
        @ApiStatus.Internal
        private static <T> Function<T, Void> convertToFunction(Consumer<T> consumer) {
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
         * @param callable callable
         * @return function
         * @param <T> the input type of the callable
         * @param <R> the return type of the callable
         */
        @ApiStatus.Internal
        private static <T, R> Function<T, R> convertToFunction(Callable<R> callable) {
            return (_ignored) -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }

    /**
     * Internal utility method to convert a {@link Callable} to a {@link Function}.
     * @param callable callable
     * @return function
     * @param <R> the return type of the callable
     */
    @ApiStatus.Internal
    private static <R> Function<Void, R> convertToFunction(Callable<R> callable) {
        return (_ignored) -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static class CancellableStageQueue implements Cancellable {
        private final AtomicBoolean isCancelledFlag;

        CancellableStageQueue(AtomicBoolean isCancelledFlag) {
            this.isCancelledFlag = isCancelledFlag;
        }

        @Override
        public void cancel() {
            isCancelledFlag.set(true);
        }
    }

    private interface Stage<I, O> {
        void execute(I input, Consumer<O> next, AtomicBoolean cancelled);
    }

    /** Async processing stage */
    private static class AsyncStage<I, O> implements Stage<I, O> {
        private final Function<I, O> function;

        AsyncStage(Function<I, O> function) {
            this.function = function;
        }

        @Override
        public void execute(I input, Consumer<O> next, AtomicBoolean cancelled) {
            if (cancelled.get()) return;

            platform.runAsync(() -> {
                if (cancelled.get()) return;

                try {
                    O result = function.apply(input);
                    next.accept(result);
                } catch (Throwable t) {
                    errorHandler.accept(t);
                }
            });
        }
    }

    /** Sync processing stage */
    private static class SyncStage<I, O> implements Stage<I, O> {
        private final Function<I, O> function;

        SyncStage(Function<I, O> function) {
            this.function = function;
        }

        @Override
        public void execute(I input, Consumer<O> next, AtomicBoolean cancelled) {
            if (cancelled.get()) return;

            platform.runSync(() -> {
                if (cancelled.get()) return;

                try {
                    O result = function.apply(input);
                    next.accept(result);
                } catch (Throwable t) {
                    errorHandler.accept(t);
                }
            });
        }
    }

    /** Delay stage implementation */
    private static class DelayStage<I> implements Stage<I, I> {
        private final long delayTicks;

        DelayStage(long delayTicks) {
            this.delayTicks = delayTicks > 0 ? delayTicks : 0;
        }

        @Override
        public void execute(I input, Consumer<I> next, AtomicBoolean cancelled) {
            if (cancelled.get()) return;

            if (delayTicks == 0) {
                next.accept(input);
                return;
            }

            platform.runSyncLater(delayTicks, () -> {
                if (!cancelled.get()) {
                    next.accept(input);
                }
            });
        }
    }
}