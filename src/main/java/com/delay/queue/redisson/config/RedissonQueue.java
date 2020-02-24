package com.delay.queue.redisson.config;

import com.delay.queue.redisson.handler.IsolationStrategy;
import com.delay.queue.redisson.message.MessageConverter;
import lombok.Getter;
import org.springframework.util.Assert;

import java.util.Objects;

public class RedissonQueue {

    @Getter
    private final String queue;
    @Getter
    private final boolean delay;
    @Getter
    private final IsolationStrategy isolationHandler;
    @Getter
    private final MessageConverter messageConverter;

    public RedissonQueue(String queue) {
        this(queue, false);
    }

    public RedissonQueue(String queue, boolean delay) {
        this(queue, delay, null);
    }

    public RedissonQueue(String queue, boolean delay, IsolationStrategy isolationHandler) {
        this(queue, delay, isolationHandler, null);
    }

    public RedissonQueue(String queue, boolean delay, IsolationStrategy isolationHandler, MessageConverter messageConverter) {
        Assert.hasText(queue, "queue name must not be empty");
        this.queue = queue;
        this.delay = delay;
        this.isolationHandler = isolationHandler;
        this.messageConverter = messageConverter;
    }

    @Override
    public int hashCode() {
        return this.queue.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RedissonQueue that = (RedissonQueue) o;
        return Objects.equals(this.queue, that.queue);
    }

}
