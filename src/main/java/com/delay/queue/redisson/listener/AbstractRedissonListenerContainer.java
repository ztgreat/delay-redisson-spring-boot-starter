package com.delay.queue.redisson.listener;

import com.delay.queue.redisson.support.ThreadFactoryCreator;
import lombok.Getter;
import lombok.Setter;
import org.redisson.api.RedissonClient;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.util.Assert;

import java.util.concurrent.Executor;

public abstract class AbstractRedissonListenerContainer implements RedissonListenerContainer {

    private final Object lifecycleMonitor = new Object();

    @Setter
    @Getter
    private Executor taskExecutor = new SimpleAsyncTaskExecutor(ThreadFactoryCreator.create("RedissonConsumeThread"));

    private final ContainerProperties containerProperties;
    @Getter
    private RedissonMessageListener<?> redissonListener;
    @Getter
    private RedissonClient redissonClient;

    @Setter
    private boolean autoStartup = true;

    @Setter
    private int phase = Integer.MAX_VALUE;

    private volatile boolean running = false;

    public AbstractRedissonListenerContainer(ContainerProperties containerProperties) {
        Assert.notNull(containerProperties, "ContainerProperties must not be null");
        this.containerProperties = containerProperties;
    }

    @Override
    public boolean isAutoStartup() {
        return this.autoStartup;
    }

    @Override
    public void stop(Runnable callback) {
        try {
            this.stop();
        } finally {
            callback.run();
        }
    }

    @Override
    public void start() {
        if (isRunning()) {
            return;
        }
        synchronized (this.lifecycleMonitor) {
            this.doStart();
            this.running = true;
        }
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            return;
        }
        synchronized (this.lifecycleMonitor) {
            this.doStop();
            this.running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public int getPhase() {
        return this.phase;
    }

    @Override
    public ContainerProperties getContainerProperties() {
        return this.containerProperties;
    }

    @Override
    public void setListener(RedissonMessageListener<?> listener) {
        Assert.notNull(listener, "RedissonMessageListener must not be null");
        this.redissonListener = listener;
    }

    @Override
    public void setRedissonClient(RedissonClient redissonClient) {
        Assert.notNull(redissonClient, "RedissonClient must not be null");
        this.redissonClient = redissonClient;
    }

    /**
     * do start
     */
    protected abstract void doStart();

    /**
     * do stop
     */
    protected abstract void doStop();


    protected enum ConsumerStatus {
        /**
         * created
         */
        CREATED,
        /**
         * running
         */
        RUNNING,
        /**
         * stopped
         */
        STOPPED
    }

}
