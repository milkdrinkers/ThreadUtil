package io.github.milkdrinkers.threadutil.task;

import io.github.milkdrinkers.threadutil.PlatformAdapter;
import io.github.milkdrinkers.threadutil.PlatformFolia;
import io.github.milkdrinkers.threadutil.exception.SchedulerMissingEntityException;
import io.github.milkdrinkers.threadutil.exception.SchedulerMissingLocationException;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Sync processing task implementation
 */
public class FoliaSyncTask<I, O> extends SyncTask<I, O> {
    private final Function<I, O> function;
    private final TaskSchedulerType schedulerType;
    private @Nullable Location location;
    private @Nullable Entity entity;

    public FoliaSyncTask(Function<I, O> function) {
        super(function);
        this.function = function;
        this.schedulerType = TaskSchedulerType.GLOBAL;
    }

    public FoliaSyncTask(@NotNull Location location, Function<I, O> function) {
        super(function);
        this.function = function;
        this.schedulerType = TaskSchedulerType.LOCATION;
        this.location = location;
    }

    public FoliaSyncTask(@NotNull Entity entity, Function<I, O> function) {
        super(function);
        this.function = function;
        this.schedulerType = TaskSchedulerType.ENTITY;
        this.entity = entity;
    }

    @Override
    public void execute(I input, Consumer<O> next, AtomicBoolean cancelled, PlatformAdapter platform, Consumer<Throwable> errorHandler) {
        if (cancelled.get()) return;

        PlatformFolia platformFolia = (PlatformFolia) platform; // You cannot end up here without the platform adapter being folia

        switch (schedulerType) {
            case GLOBAL -> {
                platform.runSync(() -> {
                    if (cancelled.get()) return;

                    try {
                        O result = function.apply(input);
                        next.accept(result);
                    } catch (Throwable t) {
                        errorHandler.accept(t);
                    }
                });
            }
            case LOCATION -> {
                if (location == null)
                    throw new SchedulerMissingLocationException("Location is null, but scheduled task type is LOCATION");

                platformFolia.runSync(location.getWorld(), location.getBlockX() >> 4, location.getBlockZ() >> 4, () -> {
                    if (cancelled.get()) return;

                    try {
                        O result = function.apply(input);
                        next.accept(result);
                    } catch (Throwable t) {
                        errorHandler.accept(t);
                    }
                });
            }
            case ENTITY -> {
                if (entity == null)
                    throw new SchedulerMissingEntityException("Entity is null, but scheduled task type is ENTITY");

                platformFolia.runSync(entity, () -> {
                    if (cancelled.get()) return;

                    try {
                        O result = function.apply(input);
                        next.accept(result);
                    } catch (Throwable t) {
                        errorHandler.accept(t);
                    }
                });
            }
        }
    }
}
