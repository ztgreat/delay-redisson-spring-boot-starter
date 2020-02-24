package com.delay.queue.redisson.consts;

public enum ListenerType {
    /**
     * take one message once
     */
    SIMPLE,
    /**
     * take list message once
     */
    BATCH
}
