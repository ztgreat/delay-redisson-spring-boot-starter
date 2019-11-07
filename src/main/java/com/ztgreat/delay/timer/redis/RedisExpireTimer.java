package com.ztgreat.delay.timer.redis;

import com.ztgreat.delay.exception.DelayTaskException;
import com.ztgreat.delay.timer.AbstractTimer;
import com.ztgreat.delay.timer.DelayedTask;
import com.ztgreat.delay.timer.TaskExecutor;
import com.ztgreat.delay.timer.TaskExpireListener;
import com.ztgreat.delay.utils.RedisLock;
import com.ztgreat.delay.utils.SerializeUtil;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

/**
 * 基于redis过期时间实现的定时任务，通过注册__keyevent@0__:expired事件，来执行对应计时的任务.
 * 精度取决于redis的过期和通知机制，从 Redis 2.6 起，过期时间误差缩小到0-1毫秒.
 *
 * @author ztgreat
 */
public class RedisExpireTimer extends AbstractTimer implements TaskExpireListener {

    /**
     * 延迟任务 前缀
     */
    static final String TASK_PREFIX = "delay:task:";

    private static final String TASK_HSET_PREFIX = "delay:task:object";

    private static final String PRESENT = "";

    private static final long DEFAULT_TASK_EXPIRE_TIME = 3600 * 1000L;

    private TaskExecutor executor = new TaskExecutor();

    private final RedisTemplate<String, byte[]> redisTemplate;

    private RedisMessageListenerContainer listenerContainer;

    private final RedisLock redisLock;

    private final Subscriber subscriber;

    public RedisExpireTimer(RedisTemplate redisTemplate, RedisMessageListenerContainer listenerContainer) {
        this.listenerContainer = listenerContainer;
        this.redisTemplate = redisTemplate;
        subscriber = new Subscriber();
        redisLock = new RedisLock(redisTemplate);
    }

    @Override
    public void doAddTask(DelayedTask task) {
        String realId = TASK_PREFIX + task.getId();
        long expire = task.getDelay() / 1000;
        boolean result = redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> redisConnection.setEx(realId.getBytes(), expire <= 0 ? 1 : expire, PRESENT.getBytes()));
        if (!result) {
            throw new DelayTaskException("failed");
        }
        redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.hSet(TASK_HSET_PREFIX.getBytes(), realId.getBytes(), SerializeUtil.serialize(task)));
    }

    @Override
    protected void onStart() {
        subscriber.start();
    }


    @Override
    protected void onStop() {
        subscriber.exit();
    }

    @Override
    public void process(String taskId) {
        byte[] bytes = redisTemplate.execute((RedisCallback<byte[]>) connection -> connection.hGet(TASK_HSET_PREFIX.getBytes(), taskId.getBytes()));
        DelayedTask task = SerializeUtil.deserialize(bytes, DelayedTask.class);
        if (task != null) {
            long lock = redisLock.lock(task.getId(), DEFAULT_TASK_EXPIRE_TIME);
            if (lock == 0) {
                return;
            }
            executor.execute(task, t -> {
                LOGGER.info("removing finished task: {}", t);
                redisLock.unlock(t.getId(), lock);
                return redisTemplate.opsForHash().delete(TASK_HSET_PREFIX, TASK_PREFIX + t.getId()) > 0;
            });
        }
    }


    private class Subscriber extends Thread {

        private RedisMsgPubSubListener jps = new RedisMsgPubSubListener(RedisExpireTimer.this);

        private static final String EXPIRE_EVENT_NAME = "__keyevent@*__:expired";

        @Override
        public void run() {
            listenerContainer.addMessageListener(jps, new PatternTopic(EXPIRE_EVENT_NAME));
            LOGGER.debug("subscribe to key expire event done");
        }

        public void exit() {
            listenerContainer.removeMessageListener(jps);
        }

    }
}
