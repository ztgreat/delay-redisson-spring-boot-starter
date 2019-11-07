package com.ztgreat.delay.timer.redis;

import com.ztgreat.delay.timer.TaskExpireListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;

import java.nio.charset.StandardCharsets;

/**
 * @author ztgreat
 */
public class RedisMsgPubSubListener implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisMsgPubSubListener.class);

    private TaskExpireListener listener;

    private static final String EXPIRE_EVENT_NAME = "__keyevent@0__:expired";

    public RedisMsgPubSubListener(TaskExpireListener listener) {
        this.listener = listener;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel(), StandardCharsets.UTF_8);
        String body = new String(message.getBody());
        LOGGER.debug("message received, channel: {}, body: {}", channel, body);
        if (EXPIRE_EVENT_NAME.equals(channel) && body.startsWith(RedisExpireTimer.TASK_PREFIX)) {
            listener.process(body);
        }
    }
}