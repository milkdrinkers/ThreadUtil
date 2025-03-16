package io.github.milkdrinkers.threadutil;

import io.github.milkdrinkers.threadutil.internal.ExecutorService;
import io.github.milkdrinkers.threadutil.internal.ExecutorServiceBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.time.Duration;

public class PlatformBukkit implements PlatformAdapter {
    private final Plugin plugin;
    private final ExecutorService executorService;

    public PlatformBukkit(Plugin plugin) {
        this.plugin = plugin;
        this.executorService = new ExecutorServiceBuilder().setImplementationName(plugin.getName()).build();
    }

    @Override
    public boolean isMainThread() {
        return Bukkit.isPrimaryThread();
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void runSync(Runnable runnable) {
        if (plugin.isEnabled()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable);
        } else {
            runnable.run();
        }
    }

    @Override
    public void runAsync(Runnable runnable) {
        PlatformAdapter.super.runAsync(runnable);
    }

    @Override
    public void runSyncLater(long ticks, Runnable runnable) {
        if (plugin.isEnabled()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, runnable, ticks);
        } else {
            runnable.run();
        }
    }

    @Override
    public void runSyncLater(Duration duration, Runnable runnable) {
        runSyncLater(toTicks(duration), runnable);
    }

    @Override
    public void shutdown(Duration duration) {
        PlatformAdapter.super.shutdown(duration);
    }

    public long toTicks(Duration duration) {
        return (duration.toMillis() + 49L) / 50L; // Round up to nearest tick
    }

    public Duration fromTicks(long ticks) {
        return Duration.ofMillis(ticks * 50L);
    }
}
