package io.github.milkdrinkers.threadutil;

import io.github.milkdrinkers.threadutil.exception.SchedulerShutdownTimeoutException;
import io.github.milkdrinkers.threadutil.internal.ExecutorService;

import java.time.Duration;

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
}
