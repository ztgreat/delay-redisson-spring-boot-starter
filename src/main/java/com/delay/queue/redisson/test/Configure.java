package com.delay.queue.redisson.test;

import com.alibaba.fastjson.JSONObject;
import com.delay.queue.redisson.annotation.RedissonListener;
import com.delay.queue.redisson.config.RedissonQueue;
import com.delay.queue.redisson.exception.MessageConversionException;
import com.delay.queue.redisson.message.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.Map;

@Configuration
public class Configure {


    @Bean(name = "testRedissonQueue")
    public RedissonQueue rivenRedissonQueue() {
        return new RedissonQueue("test", true, null, messageConverter());
    }

    @Bean(name = "test2RedissonQueue")
    public RedissonQueue redissonQueue() {
        return new RedissonQueue("test2", true, null, messageConverter());
    }


    @Bean("myMessageConverter")
    public MessageConverter messageConverter() {
        return new MessageConverter() {
            @Override
            public QueueMessage<?> toMessage(Object object, Map<String, Object> headers) throws MessageConversionException {
                //do something you want, eg:
                headers.put("my_header", "my_header_value");
                return QueueMessageBuilder.withPayload(object).headers(headers).build();
            }

            @Override
            public Object fromMessage(RedissonMessage redissonMessage) throws MessageConversionException {
                byte[] payload = redissonMessage.getPayload();
                String payloadStr = new String(payload);
                return JSONObject.parseObject(payloadStr, String.class);
            }
        };
    }

    @RedissonListener(queues = "test", messageConverter = "myMessageConverter")
    public void handler(@Header(value = RedissonHeaders.MESSAGE_ID, required = false) String messageId,
                        @Header(RedissonHeaders.DELIVERY_QUEUE_NAME) String queue,
                        @Header(RedissonHeaders.SEND_TIMESTAMP) long sendTimestamp,
                        @Header(RedissonHeaders.EXPECTED_DELAY_MILLIS) long expectedDelayMillis,
                        @Header(value = "my_header", required = false, defaultValue = "test") String myHeader,
                        @Payload String carLbsDto) {
        System.out.println("-----------------------");
        System.out.println("messageId:" + messageId);
        System.out.println("queue:" + queue);
        System.out.println("myHeader:" + myHeader);
        long actualDelay = System.currentTimeMillis() - (sendTimestamp + expectedDelayMillis);
        System.out.println("receive " + carLbsDto + ", delayed " + actualDelay + " millis");
    }

    @RedissonListener(queues = "test2", messageConverter = "myMessageConverter")
    public void handler2(@Header(value = RedissonHeaders.MESSAGE_ID, required = false) String messageId,
                         @Header(RedissonHeaders.DELIVERY_QUEUE_NAME) String queue,
                         @Header(RedissonHeaders.SEND_TIMESTAMP) long sendTimestamp,
                         @Header(RedissonHeaders.EXPECTED_DELAY_MILLIS) long expectedDelayMillis,
                         @Header(value = "my_header", required = false, defaultValue = "test") String myHeader,
                         @Payload String carLbsDto) {
        System.out.println("-----------------------");
        System.out.println("messageId:" + messageId);
        System.out.println("queue:" + queue);
        System.out.println("myHeader:" + myHeader);
        long actualDelay = System.currentTimeMillis() - (sendTimestamp + expectedDelayMillis);
        System.out.println("receive " + carLbsDto + ", delayed " + actualDelay + " millis");
    }

}
