package com.delay.queue.redisson.listener;

public class DefaultRedissonListenerContainerFactory implements RedissonListenerContainerFactory {

    @Override
    public RedissonListenerContainer createListenerContainer(ContainerProperties containerProperties) {
        int concurrency = containerProperties.getConcurrency();
        return new ConcurrentRedissonListenerContainer(containerProperties, concurrency <= 1 ? 1 : concurrency);
    }

}
