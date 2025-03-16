package io.github.milkdrinkers.threadutil;

/**
 * Interface for cancelling a running task queue
 */
public interface Cancellable {
    /**
     * Requests cancellation of the task queue
     *
     * @implNote This method does not cancel a task that is already running but prevents the remaining task queue from executing
     */
    void cancel();
}
