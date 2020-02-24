package com.delay.queue.redisson.message;

import org.springframework.util.Assert;

import java.util.Map;

public class QueueMessage<T> {

    private final T payload;

    private final Map<String, Object> headers;

    public QueueMessage(T payload, Map<String, Object> headers) {
        Assert.notNull(payload, "payload must not be null");
        this.payload = payload;
        this.headers = headers;
    }

    public T getPayload() {
        return payload;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

}
