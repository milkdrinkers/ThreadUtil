package io.github.milkdrinkers.threadutil.queue;

import io.github.milkdrinkers.threadutil.Cancellable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The representation of a running {@link TaskQueue}.
 */
public class RunningTaskQueue implements Cancellable {
    private final AtomicBoolean isCancelledFlag;

    RunningTaskQueue(AtomicBoolean isCancelledFlag) {
        this.isCancelledFlag = isCancelledFlag;
    }

    @Override
    public void cancel() {
        isCancelledFlag.set(true);
    }
}
