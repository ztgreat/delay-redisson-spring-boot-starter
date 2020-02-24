package com.delay.queue.redisson.listener;

import com.delay.queue.redisson.consts.ListenerType;
import com.delay.queue.redisson.handler.IsolationStrategy;
import com.delay.queue.redisson.handler.RedissonListenerErrorHandler;
import com.delay.queue.redisson.message.MessageConverter;
import lombok.Data;

@Data
public class ContainerProperties {

    private String queue;

    private ListenerType listenerType;

    private RedissonListenerErrorHandler errorHandler;

    private IsolationStrategy isolationStrategy;

    private MessageConverter messageConverter;

    private int concurrency;

    private int maxFetch;

}
