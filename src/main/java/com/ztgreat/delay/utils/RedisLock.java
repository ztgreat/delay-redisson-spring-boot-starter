package com.ztgreat.delay.utils;

import cn.hutool.core.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

/**
 * @author ztgreat
 */
public class RedisLock {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private String distributedLock = "distributed:locked:";
    private static final long defaultWaitTimeout = 1000;
    private RedisTemplate<String, Long> redisTemplate;

    public RedisLock(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 加锁，返回 lockId,失败返回0
     */
    public long lock(final String lockName, final Long expireTime) {
        String key = distributedLock + lockName;
        logger.info("trying to lock {}", key);
        ValueOperations<String, Long> operations = redisTemplate.opsForValue();
        long waitTime = 0L;
        do {
            // 锁时间
            long lockTimeId = this.currenttTimeFromRedis() + expireTime + RandomUtil.getRandom().nextInt(100);
            boolean hasLocked = operations.setIfAbsent(key, lockTimeId);
            if (hasLocked) {
                logger.info("lock {} success", key);
                redisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
                return lockTimeId;
            }
            Long oldLockTimeout = operations.get(key);
            if (oldLockTimeout != null && oldLockTimeout < this.currenttTimeFromRedis()) {
                Long authTimeId = operations.getAndSet(key, lockTimeId);
                // 获取上一个锁到期时间，并设置现在的锁到期时间
                if (authTimeId != null && authTimeId.equals(oldLockTimeout)) {
                    // 如过这个时候，多个线程恰好都到了这里，但是只有一个线程的设置值和当前值相同，他才有权利获取锁
                    logger.info("existing lock found,update lock {} success", key);
                    redisTemplate.expire(key, expireTime, TimeUnit.MILLISECONDS);
                    return lockTimeId;
                }
            }

            try {
                logger.info("get lock {} failed, will retry", lockName);
                TimeUnit.MILLISECONDS.sleep(100);
                waitTime += 100;
            } catch (InterruptedException e) {
                logger.error("unexpected error", e);
            }
        } while (waitTime < defaultWaitTimeout);
        return 0L;
    }

    /**
     * 解锁
     */
    public void unlock(String lockName, long lockId) {
        logger.info("trying to unlock {}:{}", lockName, lockId);
        String key = distributedLock + lockName;
        ValueOperations<String, Long> operations = redisTemplate.opsForValue();
        Long lockTimeId = operations.get(key);

        if (lockTimeId != null && lockTimeId == lockId) {
            // 如果是加锁者 则删除锁 如果不是则等待自动过期 重新竞争加锁
            redisTemplate.delete(key);
            logger.info("unlock success {}", key);
        }
    }

    /**
     * 获取当前redis时间
     */
    private long currenttTimeFromRedis() {
        return redisTemplate.execute((RedisCallback<Long>) connection -> connection.time());
    }


}
