package com.delay.queue.redisson.config;

import com.delay.queue.redisson.handler.IsolationStrategy;
import com.delay.queue.redisson.message.MessageConverter;
import lombok.Data;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;

@Data
public class QueueRegistryInfo {

    private String queueName;

    private String isolatedName;

    private RedissonQueue queue;

    private IsolationStrategy isolationHandler;

    private MessageConverter messageConverter;

    private RBlockingQueue<Object> blockingQueue;

    private RDelayedQueue<Object> delayedQueue;

    protected void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    protected void setIsolatedName(String isolatedName) {
        this.isolatedName = isolatedName;
    }

    protected void setQueue(RedissonQueue queue) {
        this.queue = queue;
    }

    protected void setIsolationHandler(IsolationStrategy isolationHandler) {
        this.isolationHandler = isolationHandler;
    }

    protected void setMessageConverter(MessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    protected void setBlockingQueue(RBlockingQueue<Object> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    protected void setDelayedQueue(RDelayedQueue<Object> delayedQueue) {
        this.delayedQueue = delayedQueue;
    }

}
