package io.github.milkdrinkers.threadutil;

import io.github.milkdrinkers.threadutil.exception.SchedulerShutdownTimeoutException;
import io.github.milkdrinkers.threadutil.internal.ExecutorService;
import io.github.milkdrinkers.threadutil.internal.ExecutorServiceBuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.ScheduledTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.BooleanSupplier;

public class PlatformSponge implements PlatformAdapter {
    private final PluginContainer plugin;
    private final ExecutorService executorService;

    public PlatformSponge(PluginContainer plugin) {
        this.plugin = plugin;
        this.executorService = new ExecutorServiceBuilder().setImplementationName(plugin.metadata().id()).build();
    }

    @Override
    public boolean isMainThread() {
        if (!Sponge.isServerAvailable())
            return false;
        return Sponge.server().onMainThread();
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void runSync(Runnable runnable) {
        final Task task = Task.builder()
            .plugin(plugin)
            .execute(runnable)
            .build();

        if (Sponge.isServerAvailable()) {
            Sponge.server().scheduler().submit(task);
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
        final Task task = Task.builder()
            .plugin(plugin)
            .execute(runnable)
            .delay(Ticks.of(ticks))
            .build();

        if (Sponge.isServerAvailable()) {
            Sponge.server().scheduler().submit(task);
        } else {
            runnable.run();
        }
    }

    @Override
    public void runSyncLater(Duration duration, Runnable runnable) {
        final Task task = Task.builder()
            .plugin(plugin)
            .execute(runnable)
            .delay(duration)
            .build();

        if (Sponge.isServerAvailable()) {
            Sponge.server().scheduler().submit(task);
        } else {
            runnable.run();
        }
    }

    @Override
    public Cancellable repeatSync(long ticks, Runnable runnable, BooleanSupplier cancellationCheck) {
        if (!Sponge.isServerAvailable()) {
            return new SpongeCancellable(null);
        }

        final Task task = Task.builder()
            .plugin(plugin)
            .execute(() -> {
                if (cancellationCheck.getAsBoolean()) {
                    return;
                }
                runnable.run();
            })
            .interval(Ticks.of(ticks))
            .build();

        final ScheduledTask scheduledTask = Sponge.server().scheduler().submit(task);
        return new SpongeCancellable(scheduledTask);
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
        return Ticks.ofWallClockTime(Sponge.server(), duration.toMillis(), ChronoUnit.MILLIS).ticks();
    }

    public Duration fromTicks(long ticks) {
        return Ticks.of(ticks).expectedDuration(Sponge.server());
    }

    private static class SpongeCancellable implements Cancellable {
        private final ScheduledTask task;

        public SpongeCancellable(ScheduledTask task) {
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
