package com.delay.queue.redisson.listener;

import com.delay.queue.redisson.message.RedissonMessage;
import com.delay.queue.redisson.message.FastJsonCodec;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;

@Slf4j
public class SimpleRedissonListenerContainer extends AbstractRedissonListenerContainer {

    public SimpleRedissonListenerContainer(ContainerProperties containerProperties) {
        super(containerProperties);
    }

    private AsyncMessageProcessingConsumer takeMessageTask;

    @Override
    protected void doStart() {
        this.takeMessageTask = new AsyncMessageProcessingConsumer();
        this.getTaskExecutor().execute(this.takeMessageTask);
    }

    @Override
    protected void doStop() {
        this.takeMessageTask.stop();
    }

    private final class AsyncMessageProcessingConsumer implements Runnable {

        private volatile Thread currentThread = null;

        private volatile ConsumerStatus status = ConsumerStatus.CREATED;

        @Override
        public void run() {
            if (this.status != ConsumerStatus.CREATED) {
                log.info("consumer currentThread [{}] will exit, because consumer status is {},expected is CREATED", this.currentThread.getName(), this.status);
                return;
            }
            final String queue = SimpleRedissonListenerContainer.this.getContainerProperties().getQueue();
            final RedissonClient redissonClient = SimpleRedissonListenerContainer.this.getRedissonClient();
            final RBlockingQueue<Object> blockingQueue = redissonClient.getBlockingQueue(queue, FastJsonCodec.INSTANCE);
            if (blockingQueue == null) {
                log.error("error occurred while create blockingQueue for queue [{}]", queue);
                return;
            }
            this.currentThread = Thread.currentThread();
            this.status = ConsumerStatus.RUNNING;
            for (; ; ) {
                try {
                    RedissonMessage redissonMessage = (RedissonMessage) blockingQueue.take();
                    SimpleRedissonMessageListenerAdapter redissonListener = (SimpleRedissonMessageListenerAdapter) SimpleRedissonListenerContainer.this.getRedissonListener();
                    redissonListener.onMessage(redissonMessage);
                } catch (InterruptedException | RedisException e) {
                    //ignore
                } catch (Exception e) {
                    log.error("error occurred while take message from redisson", e);
                }
                if (this.status == ConsumerStatus.STOPPED) {
                    log.info("consumer currentThread [{}] will exit, because of STOPPED status", this.currentThread.getName());
                    break;
                }
            }
            this.currentThread = null;
        }

        private void stop() {
            if (this.currentThread != null) {
                this.status = ConsumerStatus.STOPPED;
                this.currentThread.interrupt();
            }
        }
    }

}
