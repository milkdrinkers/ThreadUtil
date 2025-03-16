package io.github.milkdrinkers.threadutil.task;

import io.github.milkdrinkers.threadutil.PlatformAdapter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Sync processing task implementation
 */
public class SyncTask<I, O> implements Task<I, O> {
    private final Function<I, O> function;

    public SyncTask(Function<I, O> function) {
        this.function = function;
    }

    @Override
    public void execute(I input, Consumer<O> next, AtomicBoolean cancelled, PlatformAdapter platform, Consumer<Throwable> errorHandler) {
        if (cancelled.get()) return;

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
}
