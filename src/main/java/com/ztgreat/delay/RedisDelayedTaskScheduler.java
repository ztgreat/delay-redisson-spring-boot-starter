package com.ztgreat.delay;

import com.ztgreat.delay.timer.DelayedTask;
import com.ztgreat.delay.timer.redis.RedisExpireTimer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 基于redis的
 * 延迟任务 调度器
 *
 * @author ztgreat
 */
@Component
public class RedisDelayedTaskScheduler implements DelayedTaskScheduler {

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedisMessageListenerContainer redisMessageListenerContainer;

    private RedisExpireTimer expireTimer;


    @Override
    public boolean start() {
        expireTimer = new RedisExpireTimer(redisTemplate, redisMessageListenerContainer);
        expireTimer.start();
        return true;
    }

    @Override
    public boolean stop() {
        return false;
    }

    /**
     * 提交延迟任务
     *
     * @param delayedTask 延迟任务
     */
    @Override
    public boolean submitTask(DelayedTask delayedTask) {
        expireTimer.addTask(delayedTask);
        return true;
    }

}
