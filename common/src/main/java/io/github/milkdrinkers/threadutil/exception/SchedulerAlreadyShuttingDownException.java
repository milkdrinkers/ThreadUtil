package io.github.milkdrinkers.threadutil.exception;

public class SchedulerAlreadyShuttingDownException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SchedulerAlreadyShuttingDownException(String message) {
        super(message);
    }
}
