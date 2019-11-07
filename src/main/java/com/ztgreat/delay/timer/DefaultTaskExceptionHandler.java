package com.ztgreat.delay.timer;

import org.slf4j.LoggerFactory;

/**
 * 默认的 任务 执行异常 handler
 *
 * @author ztgreat
 */
public class DefaultTaskExceptionHandler implements TaskExceptionHandler {

    private static final long serialVersionUID = 1L;

    @Override
    public void handle(DelayedTask task, Throwable e) {
        LoggerFactory.getLogger(DefaultTaskExceptionHandler.class).error("failed to handle task {}", task.getId(), e);
    }
}
