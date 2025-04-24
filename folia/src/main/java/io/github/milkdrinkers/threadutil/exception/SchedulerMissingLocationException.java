package io.github.milkdrinkers.threadutil.exception;

import java.io.Serial;

public class SchedulerMissingLocationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public SchedulerMissingLocationException(String message) {
        super(message);
    }
}
