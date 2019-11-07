package com.ztgreat.delay.timer;

import cn.hutool.core.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;

/**
 * 延迟任务定义
 *
 * @author zthgreat
 */
public abstract class DelayedTask implements Serializable {


    Logger LOGGER = LoggerFactory.getLogger(DelayedTask.class);

    /**
     * 任务id
     */
    private String id;

    /**
     * 执行时间，毫秒时间戳
     */
    private long executeTime;

    /**
     * 异常handler
     */
    private TaskExceptionHandler handler;


    public DelayedTask() {
        this(IdUtil.fastUUID(), new DefaultTaskExceptionHandler());
    }

    public DelayedTask(String id) {
        this(id, new DefaultTaskExceptionHandler());
    }

    public DelayedTask(String id, TaskExceptionHandler handler) {
        this.id = id;
        this.executeTime = getDelay() + System.currentTimeMillis();
        this.handler = handler;

        LOGGER.debug("task {} will execute at {}", id, new Date(getExecuteTime()));
    }

    public String getId() {
        return id;
    }

    public abstract void run();

    public abstract long getDelay();

    /**
     * 获取执行时间点
     *
     * @return
     */
    public long getExecuteTime() {
        return executeTime;
    }

    public TaskExceptionHandler getExceptionHandler() {
        return handler;
    }

}
