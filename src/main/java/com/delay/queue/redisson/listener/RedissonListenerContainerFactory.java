package com.delay.queue.redisson.listener;

public interface RedissonListenerContainerFactory {

    RedissonListenerContainer createListenerContainer(ContainerProperties containerProperties);

}
