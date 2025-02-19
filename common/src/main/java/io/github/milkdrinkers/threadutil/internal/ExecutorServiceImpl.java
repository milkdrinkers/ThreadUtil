package io.github.milkdrinkers.threadutil.internal;

import io.github.milkdrinkers.threadutil.exception.SchedulerShutdownTimeoutException;

import java.time.Duration;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Handles the executor service for an implementation
 */
public class ExecutorServiceImpl implements ExecutorService {
    private final String implementationName;
    private final ThreadPoolExecutor threadPool;
    private final Duration shutdownTimeout;

    protected ExecutorServiceImpl(String implementationName, ThreadPoolExecutor threadPool, Duration shutdownTimeout) {
        this.implementationName = implementationName;
        this.threadPool = threadPool;
        this.shutdownTimeout = shutdownTimeout;
    }

    @Override
    public String getImplementationName() {
        return implementationName;
    }

    @Override
    public void run(Runnable runnable) {
        threadPool.submit(runnable);
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void shutdown(Duration duration) throws SchedulerShutdownTimeoutException {
        try {
            threadPool.setRejectedExecutionHandler((runnable, executor) -> runnable.run());
            threadPool.shutdown();
            if (!threadPool.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                throw new SchedulerShutdownTimeoutException("Scheduler \"" + implementationName + "\" did not shut down in time" + duration.getSeconds() + " seconds");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
