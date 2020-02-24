package com.delay.queue.redisson.listener;


public interface RedissonMessageListener<T> {

    /**
     * on message method
     *
     * @param t
     * @throws Exception
     */
    void onMessage(T t) throws Exception;

}
