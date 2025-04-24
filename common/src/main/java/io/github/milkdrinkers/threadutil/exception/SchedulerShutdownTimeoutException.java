package io.github.milkdrinkers.threadutil.exception;

public class SchedulerShutdownTimeoutException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SchedulerShutdownTimeoutException(String message) {
        super(message);
    }
}
