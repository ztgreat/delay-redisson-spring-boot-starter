package com.ztgreat.delay.timer.redis;

import cn.hutool.core.util.IdUtil;
import com.ztgreat.delay.timer.DefaultTaskExceptionHandler;
import com.ztgreat.delay.timer.DelayedTask;
import com.ztgreat.delay.timer.TaskExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 延迟任务定义
 *
 * @author zthgreat
 */
public class RedisDelayedTask extends DelayedTask {


    Logger LOGGER = LoggerFactory.getLogger(RedisDelayedTask.class);

    /**
     * 延迟时间
     */
    private long delayMs;


    public RedisDelayedTask(long delayMs) {
        this(IdUtil.fastUUID(), delayMs, new DefaultTaskExceptionHandler());
    }

    public RedisDelayedTask(String id, long delayMs) {
        this(id, delayMs, new DefaultTaskExceptionHandler());
    }

    public RedisDelayedTask(String id, long delayMs, TaskExceptionHandler handler) {
        super(id, handler);
        this.delayMs = delayMs;
        LOGGER.debug("task {} will execute at {}", id, new Date(getExecuteTime()));
    }

    @Override
    public void run() {

    }

    @Override
    public long getDelay() {
        return delayMs;
    }
}
