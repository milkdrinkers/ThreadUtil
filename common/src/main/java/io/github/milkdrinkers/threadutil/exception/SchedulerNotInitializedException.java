package io.github.milkdrinkers.threadutil.exception;

public class SchedulerNotInitializedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SchedulerNotInitializedException(String message) {
        super(message);
    }
}
