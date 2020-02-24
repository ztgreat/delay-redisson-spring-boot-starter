package com.delay.queue.redisson.config;

import com.delay.queue.redisson.listener.RedissonListenerContainer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.Lifecycle;
import org.springframework.context.SmartLifecycle;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class RedissonListenerRegistry implements SmartLifecycle {

    private List<RedissonListenerContainer> listenerContainers = new ArrayList<>(8);

    public void registerListenerContainer(RedissonListenerContainer listenerContainer) {
        this.listenerContainers.add(listenerContainer);
    }

    @PreDestroy
    public void destroy() {
        this.stop();
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        this.listenerContainers.forEach(listenerContainer -> listenerContainer.stop(callback));
    }

    @Override
    public void start() {
        this.listenerContainers.forEach(Lifecycle::start);
    }

    @Override
    public void stop() {
        this.listenerContainers.forEach(Lifecycle::stop);
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
