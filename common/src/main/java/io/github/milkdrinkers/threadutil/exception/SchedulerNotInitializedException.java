package io.github.milkdrinkers.threadutil.exception;

public class SchedulerNotInitializedException extends RuntimeException {
    public SchedulerNotInitializedException(String message) {
        super(message);
    }
}
