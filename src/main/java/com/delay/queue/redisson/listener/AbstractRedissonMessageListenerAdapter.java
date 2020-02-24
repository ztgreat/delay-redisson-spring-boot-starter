package com.delay.queue.redisson.listener;


import com.delay.queue.redisson.exception.MessageConversionException;
import com.delay.queue.redisson.message.MessageConverter;
import com.delay.queue.redisson.message.RedissonHeaders;
import com.delay.queue.redisson.message.RedissonMessage;
import com.delay.queue.redisson.message.QueueMessage;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class AbstractRedissonMessageListenerAdapter<T> implements RedissonMessageListener<T> {

    protected static class SimpleMessageConverter implements MessageConverter {

        @Override
        public QueueMessage<?> toMessage(Object payload, Map<String, Object> headers) throws MessageConversionException {
            return null;
        }

        @Override
        public String fromMessage(RedissonMessage redissonMessage) throws MessageConversionException {
            String charset = (String) redissonMessage.getHeaders().getOrDefault(RedissonHeaders.CHARSET_NAME, StandardCharsets.UTF_8.name());
            return new String(redissonMessage.getPayload(), Charset.forName(charset));
        }
    }

}
