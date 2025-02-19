package io.github.milkdrinkers.threadutil;

/**
 * Interface for cancelling an executing stage queue
 */
public interface Cancellable {
    /**
     * Requests cancellation of the stage queue
     *
     * @implNote This method does not cancel a stage that is already running but prevents the remaining stage queue from executing
     */
    void cancel();
}
