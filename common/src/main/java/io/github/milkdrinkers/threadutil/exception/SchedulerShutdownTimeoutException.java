package io.github.milkdrinkers.threadutil.exception;

public class SchedulerShutdownTimeoutException extends RuntimeException {
    public SchedulerShutdownTimeoutException(String message) {
        super(message);
    }
}
