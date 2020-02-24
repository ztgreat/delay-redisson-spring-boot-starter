package com.delay.queue.redisson.annotation;

import java.lang.annotation.*;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedissonListener {

    String[] queues();

    String errorHandler() default "";

    String isolationStrategy() default "";

    String messageConverter() default "";

    /**
     * concurrency of the consumer num
     *
     * @return
     */
    int concurrency() default 1;

    /**
     * poll a list data from redis each time if grater than 1
     *
     * @return
     */
    int maxFetch() default 1;

}