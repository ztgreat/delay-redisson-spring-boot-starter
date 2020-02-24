package com.delay.queue.redisson.annotation;

import com.delay.queue.redisson.config.EnableRedissonConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;


@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({EnableRedissonConfiguration.class})
public @interface EnableRedisson {

}
