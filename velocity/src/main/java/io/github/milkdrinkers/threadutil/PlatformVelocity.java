package io.github.milkdrinkers.threadutil;

import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.TaskStatus;
import io.github.milkdrinkers.threadutil.exception.SchedulerShutdownTimeoutException;
import io.github.milkdrinkers.threadutil.internal.ExecutorService;
import io.github.milkdrinkers.threadutil.internal.ExecutorServiceBuilder;

import java.time.Duration;
import java.util.function.BooleanSupplier;

public class PlatformVelocity implements PlatformAdapter {
    private final Plugin plugin;
    private final ExecutorService executorService;
    private final ProxyServer server;

    public PlatformVelocity(Plugin plugin, ProxyServer server) {
        this.plugin = plugin;
        this.executorService = new ExecutorServiceBuilder().setImplementationName(plugin.name()).build();
        this.server = server;
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
        server.getScheduler()
            .buildTask(plugin, runnable)
            .schedule();
    }

    @Override
    public void runAsync(Runnable runnable) {
        PlatformAdapter.super.runAsync(runnable);
    }

    @Override
    public void runSyncLater(Duration duration, Runnable runnable) {
        server.getScheduler()
            .buildTask(plugin, runnable)
            .delay(duration)
            .schedule();
    }

    @Override
    public void runSyncLater(long ticks, Runnable runnable) {
        server.getScheduler()
            .buildTask(plugin, runnable)
            .delay(fromTicks(ticks))
            .schedule();
    }

    @Override
    public Cancellable repeatSync(long ticks, Runnable runnable, BooleanSupplier cancellationCheck) {
        final ScheduledTask task = server.getScheduler()
            .buildTask(plugin, () -> {
                if (cancellationCheck.getAsBoolean()) {
                    return;
                }
                runnable.run();
            })
            .repeat(fromTicks(ticks))
            .schedule();

        return new VelocityCancellable(task);
    }

    @Override
    public Cancellable repeatAsync(long ticks, Runnable runnable, BooleanSupplier cancellationCheck) {
        return PlatformAdapter.super.repeatAsync(ticks, runnable, cancellationCheck);
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

    private static class VelocityCancellable implements Cancellable {
        private final ScheduledTask task;

        public VelocityCancellable(ScheduledTask task) {
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
            return task == null || task.status() == TaskStatus.CANCELLED;
        }
    }
}