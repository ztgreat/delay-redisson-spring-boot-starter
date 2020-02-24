package com.delay.queue.redisson.handler;

public interface IsolationStrategy {

    /**
     * acquire queue name apply to redis
     *
     * @param queue
     * @return
     */
    String getRedisQueueName(String queue);

}
