package com.ztgreat.delay.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author ztgreat
 */
public abstract class AbstractTimer {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /**
     * 任务状态
     */
    private static final int WORKER_STATE_INIT = 0;
    private static final int WORKER_STATE_STARTED = 1;
    private static final int WORKER_STATE_SHUTDOWN = 2;

    /**
     * 0 - init, 1 - started, 2 - shut down
     */
    private volatile int workerState = WORKER_STATE_INIT;

    protected static final AtomicIntegerFieldUpdater<AbstractTimer> WORKER_STATE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(AbstractTimer.class, "workerState");

    public void addTask(DelayedTask job) {
        if (job == null) {
            throw new NullPointerException("job");
        }
        if (job.getDelay() < 0) {
            throw new IllegalArgumentException("delay must be >= 0");
        }
        doAddTask(job);
    }

    /**
     * 添加延迟 任务
     *
     * @param job 延迟任务
     */
    protected abstract void doAddTask(DelayedTask job);

    public void start() {
        switch (WORKER_STATE_UPDATER.get(this)) {
            case WORKER_STATE_INIT:
                if (WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_INIT, WORKER_STATE_STARTED)) {
                    onStart();
                }
                break;
            case WORKER_STATE_STARTED:
                break;
            case WORKER_STATE_SHUTDOWN:
                throw new IllegalStateException("cannot be started once stopped");
            default:
                throw new Error("Invalid WorkerState");
        }
    }

    protected abstract void onStart();

    /**
     * 停止
     */
    public void stop() {
        if (WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_STARTED, WORKER_STATE_SHUTDOWN)) {
            onStop();
        }
    }

    protected abstract void onStop();
}
