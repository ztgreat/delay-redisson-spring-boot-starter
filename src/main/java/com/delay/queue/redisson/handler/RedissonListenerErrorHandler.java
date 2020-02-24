package com.delay.queue.redisson.handler;

import com.delay.queue.redisson.message.RedissonMessage;
import org.springframework.messaging.Message;

@FunctionalInterface
public interface RedissonListenerErrorHandler {

    /**
     * error handler
     *
     * @param message   redisson message
     * @param throwable throwable
     */
    void handleError(RedissonMessage message, Message<?> messagingMessage, Throwable throwable);

}
