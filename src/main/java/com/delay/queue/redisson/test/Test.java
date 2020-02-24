package com.delay.queue.redisson.test;

import com.delay.queue.redisson.config.RedissonTemplate;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.locks.LockSupport;

@SpringBootTest
@RunWith(SpringRunner.class)
public class Test {


    @Autowired
    private RedissonTemplate redissonTemplate;

    @org.junit.Test
    public void test() {
        System.out.println("准备发送延迟消息");
        redissonTemplate.sendWithDelay("test", "helloworld", 5000);
        redissonTemplate.sendWithDelay("test2", "helloworld", 10000);
        LockSupport.park();
    }

}
