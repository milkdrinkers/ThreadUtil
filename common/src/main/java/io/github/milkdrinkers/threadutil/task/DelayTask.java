package io.github.milkdrinkers.threadutil.task;

import io.github.milkdrinkers.threadutil.PlatformAdapter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Delay task implementation
 */
public class DelayTask<I> implements Task<I, I> {
    private final long delayTicks;

    public DelayTask(long delayTicks) {
        this.delayTicks = delayTicks > 0 ? delayTicks : 0;
    }

    @Override
    public void execute(I input, Consumer<I> next, AtomicBoolean cancelled, PlatformAdapter platform, Consumer<Throwable> errorHandler) {
        if (cancelled.get()) return;

        if (delayTicks == 0) {
            next.accept(input);
            return;
        }

        platform.runSyncLater(delayTicks, () -> {
            if (!cancelled.get()) {
                next.accept(input);
            }
        });
    }
}
