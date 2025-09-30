package io.github.milkdrinkers.threadutil.internal;

import io.github.milkdrinkers.threadutil.exception.SchedulerShutdownTimeoutException;

import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Handles the executor service for an implementation
 */
public class ExecutorServiceImpl implements ExecutorService {
    private final String implementationName;
    private final ThreadPoolExecutor threadPool;
    private final ScheduledThreadPoolExecutor scheduledThreadPool;
    private final Duration shutdownTimeout;

    protected ExecutorServiceImpl(String implementationName, ThreadPoolExecutor threadPool, Duration shutdownTimeout) {
        this.implementationName = implementationName;
        this.threadPool = threadPool;
        this.scheduledThreadPool = new ScheduledThreadPoolExecutor(1);
        this.scheduledThreadPool.setRemoveOnCancelPolicy(true);
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
    public void runLater(Runnable runnable, Duration delay) {
        scheduledThreadPool.schedule(() -> threadPool.submit(runnable), delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    @SuppressWarnings("CallToPrintStackTrace")
    public void shutdown(Duration duration) throws SchedulerShutdownTimeoutException {
        try {
            threadPool.setRejectedExecutionHandler((runnable, executor) -> runnable.run());
            threadPool.shutdown();
            scheduledThreadPool.shutdown();

            final boolean threadPoolShutdown = threadPool.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
            final boolean scheduledShutdown = scheduledThreadPool.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);

            if (!threadPoolShutdown || !scheduledShutdown) {
                throw new SchedulerShutdownTimeoutException("Scheduler \"" + implementationName + "\" did not shut down in time: " + duration.getSeconds() + " seconds");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}