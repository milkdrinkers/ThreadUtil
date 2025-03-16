package io.github.milkdrinkers.threadutil.exception;

public class SchedulerAlreadyShuttingDownException extends RuntimeException {
    public SchedulerAlreadyShuttingDownException(String message) {
        super(message);
    }
}
