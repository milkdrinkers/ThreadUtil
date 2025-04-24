package io.github.milkdrinkers.threadutil.exception;

public class SchedulerInitializationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SchedulerInitializationException(String message) {
        super(message);
    }
}
