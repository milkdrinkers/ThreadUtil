package io.github.milkdrinkers.threadutil;

import io.github.milkdrinkers.threadutil.exception.SchedulerShutdownTimeoutException;
import io.github.milkdrinkers.threadutil.internal.ExecutorService;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/**
 * A platform adapter provides a way for ThreadUtil to integrate natively with a platform.
 *
 * @see Scheduler#init(PlatformAdapter)
 */
public interface PlatformAdapter {
    /**
     * Returns whether this is running on the {@link PlatformAdapter}'s main thread
     *
     * @return boolean
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isMainThread();

    /**
     * Returns the executor service for this {@link PlatformAdapter}
     *
     * @return executor service
     */
    ExecutorService getExecutorService();

    /**
     * Schedule a runnable to be executed synchronously (On the main thread)
     *
     * @param runnable runnable
     */
    void runSync(Runnable runnable);

    /**
     * Schedule a runnable to be executed asynchronously (On a random thread)
     *
     * @param runnable runnable
     */
    default void runAsync(Runnable runnable) {
        getExecutorService().run(runnable);
    }

    /**
     * Schedule a runnable to be executed synchronously after a delay (On the main thread)
     *
     * @param duration duration
     * @param runnable runnable
     */
    void runSyncLater(Duration duration, Runnable runnable);

    /**
     * Schedule a runnable to be executed synchronously after a delay (On the main thread)
     *
     * @param ticks    duration
     * @param runnable runnable
     */
    void runSyncLater(long ticks, Runnable runnable);

    /**
     * Schedule a runnable to be executed repeatedly synchronously on the main thread.
     * The task will continue executing until the cancellationCheck returns true.
     *
     * @param ticks             interval between executions in ticks
     * @param runnable          the task to execute
     * @param cancellationCheck supplier that returns true when the task should be cancelled
     * @return a Cancellable handle to stop the repeating task
     */
    Cancellable repeatSync(long ticks, Runnable runnable, BooleanSupplier cancellationCheck);

    /**
     * Schedule a runnable to be executed repeatedly asynchronously.
     * The task will continue executing until the cancellationCheck returns true.
     *
     * @param ticks             interval between executions in ticks
     * @param runnable          the task to execute
     * @param cancellationCheck supplier that returns true when the task should be cancelled
     * @return a Cancellable handle to stop the repeating task
     */
    default Cancellable repeatAsync(long ticks, Runnable runnable, BooleanSupplier cancellationCheck) {
        final AtomicBoolean cancelled = new java.util.concurrent.atomic.AtomicBoolean(false);

        final Runnable repeater = new Runnable() {
            @Override
            public void run() {
                if (cancelled.get() || cancellationCheck.getAsBoolean()) {
                    return;
                }

                try {
                    runnable.run();
                } finally {
                    if (!cancelled.get() && !cancellationCheck.getAsBoolean()) { // Schedule next iteration if not cancelled
                        getExecutorService().runLater(this, fromTicks(ticks));
                    }
                }
            }
        };

        // Start the first iteration immediately
        getExecutorService().run(repeater);

        return new Cancellable() {
            @Override
            public void cancel() {
                cancelled.set(true);
            }

            @Override
            public boolean isCancelled() {
                return cancelled.get();
            }
        };
    }

    /**
     * A method executed when the {@link PlatformAdapter} is shutting down
     *
     * @param duration duration
     * @throws SchedulerShutdownTimeoutException thrown if the scheduler is not shut down in time
     */
    default void shutdown(Duration duration) throws SchedulerShutdownTimeoutException {
        getExecutorService().shutdown(duration);
    }

    /**
     * A method to convert duration into Minecraft ticks (20 per second).
     * @param duration duration
     * @return ticks
     */
    long toTicks(Duration duration);

    /**
     * A method to convert Minecraft ticks (20 per second) into a Duration
     * @param ticks ticks
     * @return duration
     */
    Duration fromTicks(long ticks);

    /**
     * Interface for cancellable tasks
     */
    interface Cancellable {
        /**
         * Cancel the repeating task
         */
        void cancel();

        /**
         * Check if the task has been cancelled
         * @return true if cancelled
         */
        boolean isCancelled();
    }
}