package io.github.milkdrinkers.threadutil.task;

import io.github.milkdrinkers.threadutil.PlatformAdapter;
import io.github.milkdrinkers.threadutil.TaskContext;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Loop task implementation that repeats execution at regular intervals using a platform.
 */
public class LoopTask<I> implements Task<I, I> {
    private final BiConsumer<I, TaskContext> consumer;
    private final long delayTicks;
    private final boolean isSync;

    public LoopTask(BiConsumer<I, TaskContext> consumer, long delayTicks, boolean isSync) {
        this.consumer = consumer;
        this.delayTicks = delayTicks;
        this.isSync = isSync;
    }

    @Override
    public void execute(I input, Consumer<I> next, AtomicBoolean cancelled, PlatformAdapter platform, Consumer<Throwable> errorHandler) {
        if (cancelled.get()) return;

        final TaskContext context = new TaskContext(cancelled);
        final AtomicReference<PlatformAdapter.Cancellable> taskHandle = new AtomicReference<>();

        final Runnable iteration = () -> {
            if (cancelled.get()) {
                if (taskHandle.get() != null)
                    taskHandle.get().cancel(); // Cancel the platform's repeating task
                return;
            }

            try {
                consumer.accept(input, context);

                // Check if cancelled after execution
                if (cancelled.get() && taskHandle.get() != null)
                    taskHandle.get().cancel();
            } catch (Throwable t) {
                errorHandler.accept(t);
                cancelled.set(true);
                if (taskHandle.get() != null)
                    taskHandle.get().cancel();
            }
        };

        if (isSync) {
            taskHandle.set(platform.repeatSync(delayTicks, iteration, cancelled::get));
        } else {
            taskHandle.set(platform.repeatAsync(delayTicks, iteration, cancelled::get));
        }
    }
}