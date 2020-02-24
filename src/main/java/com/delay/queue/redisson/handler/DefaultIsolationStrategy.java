package com.delay.queue.redisson.handler;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class DefaultIsolationStrategy implements IsolationStrategy {

    @Override
    public String getRedisQueueName(String queue) {
        String prefix;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String hostAddress = localHost.getHostAddress();
            String hostName = localHost.getHostName();
            prefix = hostName + "@" + hostAddress;
        } catch (UnknownHostException e) {
            log.warn("can not detect host info,instead with localhost@127.0.0.1");
            prefix = "localhost@127.0.0.1";
        }
        return prefix + "-" + queue;
    }

}
