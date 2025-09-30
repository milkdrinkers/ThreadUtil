package io.github.milkdrinkers.threadutil;

import io.github.milkdrinkers.threadutil.exception.SchedulerShutdownTimeoutException;
import io.github.milkdrinkers.threadutil.internal.ExecutorService;
import io.github.milkdrinkers.threadutil.internal.ExecutorServiceBuilder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.function.BooleanSupplier;

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
    public Cancellable repeatSync(long ticks, Runnable runnable, BooleanSupplier cancellationCheck) {
        if (!plugin.isEnabled()) {
            return new BukkitCancellable(null);
        }

        final BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (cancellationCheck.getAsBoolean()) {
                return; // Cancelled by the wrapper
            }
            runnable.run();
        }, 0L, ticks);

        return new BukkitCancellable(task);
    }

    @Override
    public Cancellable repeatAsync(long ticks, Runnable runnable, BooleanSupplier cancellationCheck) {
        if (!plugin.isEnabled()) {
            return new BukkitCancellable(null);
        }

        final BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (cancellationCheck.getAsBoolean()) {
                return; // Cancelled by the wrapper
            }
            runnable.run();
        }, 0L, ticks);

        return new BukkitCancellable(task);
    }

    @Override
    public void shutdown(Duration duration) throws SchedulerShutdownTimeoutException {
        PlatformAdapter.super.shutdown(duration);
    }

    public long toTicks(Duration duration) {
        return (duration.toMillis() + 49L) / 50L; // Round up to nearest tick
    }

    public Duration fromTicks(long ticks) {
        return Duration.ofMillis(ticks * 50L);
    }

    private static class BukkitCancellable implements Cancellable {
        private final BukkitTask task;

        public BukkitCancellable(BukkitTask task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            if (task != null) {
                task.cancel();
            }
        }

        @Override
        public boolean isCancelled() {
            return task == null || task.isCancelled();
        }
    }
}