package io.github.milkdrinkers.threadutil.exception;

import java.io.Serial;

public class SchedulerMissingEntityException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public SchedulerMissingEntityException(String message) {
        super(message);
    }
}
