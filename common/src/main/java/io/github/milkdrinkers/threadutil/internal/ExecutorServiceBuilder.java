package io.github.milkdrinkers.threadutil.internal;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ExecutorServiceBuilder {
    private String implementationName = "";
    private @Nullable ThreadPoolExecutor threadPool;
    private @Nullable Duration shutdownTimeout;

    @SuppressWarnings("UnusedReturnValue")
    public ExecutorServiceBuilder setImplementationName(String implementationName) {
        this.implementationName = implementationName;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ExecutorServiceBuilder setThreadPool(ThreadPoolExecutor threadPool) {
        this.threadPool = threadPool;
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public ExecutorServiceBuilder setShutdownTimeout(Duration shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
        return this;
    }

    public ExecutorServiceImpl build() {
        if (threadPool == null) {
            setThreadPool((ThreadPoolExecutor) Executors.newCachedThreadPool(new ThreadFactoryImpl(implementationName)));
        }

        if (shutdownTimeout == null) {
            setShutdownTimeout(Duration.ofSeconds(60));
        }

        return new ExecutorServiceImpl(implementationName, threadPool, shutdownTimeout);
    }
}