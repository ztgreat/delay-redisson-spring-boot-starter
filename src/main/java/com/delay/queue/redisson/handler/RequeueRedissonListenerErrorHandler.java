package com.delay.queue.redisson.handler;

import com.alibaba.fastjson.JSONObject;
import com.delay.queue.redisson.config.RedissonTemplate;
import com.delay.queue.redisson.message.RedissonHeaders;
import com.delay.queue.redisson.message.RedissonMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RequeueRedissonListenerErrorHandler implements RedissonListenerErrorHandler {

    private static final long DEFAULT_MAX_REQUEUE_TIMES = 1000;

    private final RedissonTemplate redissonTemplate;

    private final long maxRequeueTimes;

    public RequeueRedissonListenerErrorHandler(RedissonTemplate redissonTemplate) {
        this(redissonTemplate, DEFAULT_MAX_REQUEUE_TIMES);
    }

    public RequeueRedissonListenerErrorHandler(RedissonTemplate redissonTemplate, long maxRequeueTimes) {
        Assert.notNull(redissonTemplate, "redissonTemplate must not be null");
        Assert.isTrue(maxRequeueTimes > 0, "maxRequeueTimes must be positive");
        this.redissonTemplate = redissonTemplate;
        this.maxRequeueTimes = maxRequeueTimes;
    }

    @Override
    public void handleError(RedissonMessage message, Message<?> messagingMessage, Throwable throwable) {
        Object payload = messagingMessage.getPayload();
        if (message == null && payload instanceof List) {
            List<?> payloadList = (List<?>) payload;
            List<Map<String, Object>> batchHeaders = (List) messagingMessage.getHeaders().get(RedissonHeaders.BATCH_CONVERTED_HEADERS);
            for (int i = 0; i < payloadList.size(); i++) {
                Object payloadToRequeue = payloadList.get(i);
                Map<String, Object> rawHeaders = batchHeaders.get(i);
                this.requeue(payloadToRequeue, new HashMap<>(rawHeaders), throwable);
            }
            return;
        }
        this.requeue(payload, new HashMap<>(messagingMessage.getHeaders()), throwable);
    }

    private void requeue(Object payload, Map<String, Object> headers, Throwable throwable) {
        final String queueName = (String) headers.get(RedissonHeaders.DELIVERY_QUEUE_NAME);
        if (!StringUtils.hasText(queueName)) {
            log.warn("message [{}] delivery queue name is empty, abandon it", JSONObject.toJSONString(payload), throwable);
            return;
        }
        Long requeueTimes = this.getLongVal(headers.get(RedissonHeaders.REQUEUE_TIMES));
        if (requeueTimes < this.maxRequeueTimes) {
            headers.put(RedissonHeaders.REQUEUE_TIMES, ++requeueTimes);
        } else {
            log.warn("message [{}] reach the max requeue times, abandon it", JSONObject.toJSONString(payload), throwable);
            return;
        }
        final Long delay = this.getLongVal(headers.get(RedissonHeaders.EXPECTED_DELAY_MILLIS));
        //present as delay message
        if (delay > 0) {
            this.redissonTemplate.sendWithDelay(queueName, payload, headers, delay);
            return;
        }
        this.redissonTemplate.send(queueName, payload, headers);
    }

    private Long getLongVal(Object target) {
        if (target == null) {
            return 0L;
        }
        if (target instanceof Number) {
            return ((Number) target).longValue();
        }
        return 0L;
    }

}
