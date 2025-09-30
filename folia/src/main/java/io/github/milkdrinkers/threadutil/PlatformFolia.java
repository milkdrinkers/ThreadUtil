package io.github.milkdrinkers.threadutil;

import io.github.milkdrinkers.threadutil.exception.SchedulerShutdownTimeoutException;
import io.github.milkdrinkers.threadutil.internal.ExecutorService;
import io.github.milkdrinkers.threadutil.internal.ExecutorServiceBuilder;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class PlatformFolia implements PlatformAdapter {
    private final Plugin plugin;
    private final ExecutorService executorService;

    public PlatformFolia(Plugin plugin) {
        this.plugin = plugin;
        this.executorService = new ExecutorServiceBuilder().setImplementationName(plugin.getName()).build();
    }

    @Override
    public boolean isMainThread() {
        return plugin.getServer().isGlobalTickThread();
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void runSync(Runnable runnable) {
        if (plugin.isEnabled()) {
            plugin.getServer().getGlobalRegionScheduler().run(plugin, (_task) -> runnable.run());
        } else {
            runnable.run();
        }
    }

    public void runSync(World world, int chunkX, int chunkZ, Runnable runnable) {
        if (plugin.isEnabled()) {
            plugin.getServer().getRegionScheduler().run(plugin, world, chunkX, chunkZ, (_task) -> runnable.run());
        } else {
            runnable.run();
        }
    }

    public void runSync(Entity entity, Runnable runnable) {
        if (plugin.isEnabled()) {
            entity.getScheduler().run(plugin, (_task) -> runnable.run(), runnable);
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
            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, (_task) -> runnable.run(), ticks);
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
            return new PaperCancellable(null);
        }

        final ScheduledTask task = plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, (t) -> {
            if (cancellationCheck.getAsBoolean()) {
                return; // Cancelled by the wrapper
            }
            runnable.run();
        }, 0L, ticks);

        return new PaperCancellable(task);
    }

    @Override
    public Cancellable repeatAsync(long ticks, Runnable runnable, BooleanSupplier cancellationCheck) {
        if (!plugin.isEnabled()) {
            return new PaperCancellable(null);
        }

        final ScheduledTask task = plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, (t) -> {
            if (cancellationCheck.getAsBoolean()) {
                return; // Cancelled by the wrapper
            }
            runnable.run();
        }, 0L, ticks * 50L, TimeUnit.MILLISECONDS);

        return new PaperCancellable(task);
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

    private static class PaperCancellable implements Cancellable {
        private final ScheduledTask task;

        public PaperCancellable(ScheduledTask task) {
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
