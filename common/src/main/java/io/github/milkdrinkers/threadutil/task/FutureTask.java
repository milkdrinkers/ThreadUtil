package io.github.milkdrinkers.threadutil.task;

import io.github.milkdrinkers.threadutil.PlatformAdapter;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Future task implementation that handles CompletableFuture execution
 */
public class FutureTask<I, O> implements Task<I, O> {
    private final Function<I, CompletableFuture<O>> futureSupplier;
    private final boolean isSync;

    public FutureTask(Function<I, CompletableFuture<O>> futureSupplier, boolean isSync) {
        this.futureSupplier = futureSupplier;
        this.isSync = isSync;
    }

    @Override
    public void execute(I input, Consumer<O> next, AtomicBoolean cancelled, PlatformAdapter platform, Consumer<Throwable> errorHandler) {
        if (cancelled.get()) return;

        final Runnable execution = () -> {
            if (cancelled.get()) return;

            try {
                final CompletableFuture<O> future = futureSupplier.apply(input);

                future.whenComplete((result, throwable) -> {
                    if (cancelled.get()) return;

                    if (throwable != null) {
                        errorHandler.accept(throwable);
                    } else {
                        next.accept(result);
                    }
                });
            } catch (Throwable t) {
                errorHandler.accept(t);
            }
        };

        if (isSync) {
            platform.runSync(execution);
        } else {
            platform.runAsync(execution);
        }
    }
}