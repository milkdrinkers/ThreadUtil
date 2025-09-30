package io.github.milkdrinkers.threadutil;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Context object passed to tasks that allows them to cancel the entire task queue.
 */
public class TaskContext {
    private final AtomicBoolean isCancelledFlag;

    public TaskContext(AtomicBoolean isCancelledFlag) {
        this.isCancelledFlag = isCancelledFlag;
    }

    /**
     * Cancels the entire task queue, preventing any subsequent tasks from executing.
     *
     * @implNote This does not stop the currently executing task, but prevents
     * all remaining tasks in the queue from running.
     */
    public void cancel() {
        isCancelledFlag.set(true);
    }

    /**
     * Checks if the task queue has been cancelled.
     *
     * @return true if the task queue is cancelled
     */
    public boolean isCancelled() {
        return isCancelledFlag.get();
    }
}