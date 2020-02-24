package com.delay.queue.redisson.message;

import com.delay.queue.redisson.exception.MessageConversionException;

import java.util.Map;

public interface MessageConverter {

    /**
     * convert payload and headers to message that can send to redis queue directly
     *
     * @param payload payload
     * @param headers headers
     * @return
     */
    QueueMessage<?> toMessage(Object payload, Map<String, Object> headers) throws MessageConversionException;

    /**
     * convert redisson message to required object
     *
     * @param redissonMessage
     * @return
     */
    Object fromMessage(RedissonMessage redissonMessage) throws MessageConversionException;

}
