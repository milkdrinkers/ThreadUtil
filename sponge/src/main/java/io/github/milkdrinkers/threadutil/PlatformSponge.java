package io.github.milkdrinkers.threadutil;

import io.github.milkdrinkers.threadutil.internal.ExecutorService;
import io.github.milkdrinkers.threadutil.internal.ExecutorServiceBuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.plugin.PluginContainer;

import java.time.Duration;

public class PlatformSponge implements Platform {
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
        Platform.super.runAsync(runnable);
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
    public void shutdown(Duration duration) {
        Platform.super.shutdown(duration);
    }

    public long toTicks(Duration duration) {
        return (duration.toMillis() + 49L) / 50L; // Round up to nearest tick
    }

    public Duration fromTicks(long ticks) {
        return Duration.ofMillis(ticks * 50L);
    }
}
