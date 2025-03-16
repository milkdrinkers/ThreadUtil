package io.github.milkdrinkers.threadutil.task;

import io.github.milkdrinkers.threadutil.PlatformAdapter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * A simple representation of a task
 *
 * @param <I> input for the task
 * @param <O> output of the task
 */
public interface Task<I, O> {
    void execute(I input, Consumer<O> next, AtomicBoolean cancelled, PlatformAdapter platform, Consumer<Throwable> errorHandler);
}
