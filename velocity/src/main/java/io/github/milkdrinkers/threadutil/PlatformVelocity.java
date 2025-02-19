package io.github.milkdrinkers.threadutil;

import com.velocitypowered.api.plugin.Plugin;
import io.github.milkdrinkers.threadutil.internal.ExecutorService;
import io.github.milkdrinkers.threadutil.internal.ExecutorServiceBuilder;

import java.time.Duration;

public class PlatformVelocity implements Platform {
    private final Plugin plugin;
    private final ExecutorService executorService;

    public PlatformVelocity(Plugin plugin) {
        this.plugin = plugin;
        this.executorService = new ExecutorServiceBuilder().setImplementationName(plugin.name()).build();
    }

    @Override
    public boolean isMainThread() {
        return false;
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void runSync(Runnable runnable) {
        runnable.run();
    }

    @Override
    public void runAsync(Runnable runnable) {
        Platform.super.runAsync(runnable);
    }

    @Override
    public void runSyncLater(Duration duration, Runnable runnable) {
        runnable.run();
    }

    @Override
    public void runSyncLater(long ticks, Runnable runnable) {
        runnable.run();
    }

    @Override
    public void shutdown(Duration duration) {
        Platform.super.shutdown(duration);
    }
}
