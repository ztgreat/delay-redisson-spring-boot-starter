package com.ztgreat.delay.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class TaskExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecutor.class);

    private ExecutorService executor = Executors.newFixedThreadPool(10);

    public void execute(DelayedTask task, Function<DelayedTask, Boolean> callback) {
        if (task != null) {
            executor.execute(() -> {
                try {
                    //反序列化后LOGGER可能不起作用
                    task.LOGGER = LoggerFactory.getLogger(DelayedTask.class);
                    task.run();
                    callback.apply(task);
                } catch (Exception e) {
                    try {
                        task.getExceptionHandler().handle(task, e);
                    } catch (Throwable e1) {
                        LOGGER.error("exception from exceptionhandler", e1);
                    }
                }
            });
        }

    }

    public void terminate() {
        if (!executor.isShutdown()) {
            executor.shutdown();
        }
        if (!executor.isTerminated()) {
            try {
                executor.awaitTermination(120, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("unexpected error", e);
            }
        }
    }
}
