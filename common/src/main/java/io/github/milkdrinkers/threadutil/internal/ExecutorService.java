package io.github.milkdrinkers.threadutil.internal;

import io.github.milkdrinkers.threadutil.exception.SchedulerShutdownTimeoutException;
import org.jetbrains.annotations.ApiStatus;

import java.time.Duration;

public interface ExecutorService {
    /**
     * Gets the implementation name
     * @return implementation name
     */
    @ApiStatus.Internal
    String getImplementationName();

    /**
     * Submits a runnable to be executed by the thread pool
     * @param runnable runnable
     */
    @ApiStatus.Internal
    void run(Runnable runnable);

    /**
     * Logic run on shutdown of the implementation platform
     * @param duration maximum shutdown duration
     * @throws SchedulerShutdownTimeoutException thrown if the scheduler is not shut down in time
     */
    @ApiStatus.Internal
    void shutdown(Duration duration) throws SchedulerShutdownTimeoutException;
}
