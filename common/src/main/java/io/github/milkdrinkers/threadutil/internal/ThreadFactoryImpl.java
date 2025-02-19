package io.github.milkdrinkers.threadutil.internal;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

class ThreadFactoryImpl implements ThreadFactory {
    private final AtomicInteger threadId = new AtomicInteger();
    private final String implementationName;

    ThreadFactoryImpl(String implementationName) {
        this.implementationName = implementationName;
    }

    public int getThreadId() {
        return threadId.incrementAndGet();
    }

    public String getImplementationName() {
        return implementationName;
    }

    @Override
    public Thread newThread(@NotNull Runnable runnable) {
        return new Thread(runnable, getImplementationName() + " Thread: #" + getThreadId());
    }
}
