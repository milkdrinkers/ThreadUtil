package io.github.milkdrinkers.threadutil;

import io.github.milkdrinkers.threadutil.exception.SchedulerShutdownTimeoutException;
import io.github.milkdrinkers.threadutil.internal.ExecutorService;
import io.github.milkdrinkers.threadutil.internal.ExecutorServiceBuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public class PlatformSponge implements PlatformAdapter {
    private final PluginContainer plugin;
    private final ExecutorService executorService;

    public PlatformSponge(PluginContainer plugin) {
        this.plugin = plugin;
        this.executorService = new ExecutorServiceBuilder().setImplementationName(plugin.getName()).build();
    }

    @Override
    public boolean isMainThread() {
        if (!Sponge.isServerAvailable())
            return false;
        return Sponge.getServer().isMainThread();
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void runSync(Runnable runnable) {
        if (Sponge.isServerAvailable()) {
            Sponge.getScheduler()
                .createTaskBuilder()
                .execute(runnable)
                .submit(plugin);
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
        runSyncLater(fromTicks(ticks), runnable);
    }

    @Override
    public void runSyncLater(Duration duration, Runnable runnable) {
        if (Sponge.isServerAvailable()) {
            Sponge.getScheduler()
                .createTaskBuilder()
                .execute(runnable)
                .delay(duration.toMillis(), TimeUnit.MILLISECONDS)
                .submit(plugin);
        } else {
            runnable.run();
        }
    }

    @Override
    public Cancellable repeatSync(long ticks, Runnable runnable, BooleanSupplier cancellationCheck) {
        if (!Sponge.isServerAvailable()) {
            return new SpongeCancellable(null);
        }

        final Task task = Sponge.getScheduler()
            .createTaskBuilder()
            .intervalTicks(ticks)
            .execute(() -> {
                if (cancellationCheck.getAsBoolean()) {
                    return;
                }
                runnable.run();
            })
            .submit(plugin);

        return new SpongeCancellable(task);
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

    private static class SpongeCancellable implements Cancellable {
        private Task task;

        public SpongeCancellable(Task task) {
            this.task = task;
        }

        @Override
        public void cancel() {
            if (task != null) {
                task.cancel();
                task = null;
            }
        }

        @Override
        public boolean isCancelled() {
            return task == null;
        }
    }
}
