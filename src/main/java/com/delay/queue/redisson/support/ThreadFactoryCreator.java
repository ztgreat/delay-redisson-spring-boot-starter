package com.delay.queue.redisson.support;

import org.springframework.util.StringUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class ThreadFactoryCreator {

    public static ThreadFactory create(String threadName) {
        if (!StringUtils.hasText(threadName)) {
            throw new IllegalArgumentException("argument [threadName] must not be blank");
        }
        return new NamedWithIdThreadFactory(threadName);
    }

    private static final class NamedWithIdThreadFactory implements ThreadFactory {

        private AtomicInteger threadId = new AtomicInteger(1);

        private String namePrefix;

        private NamedWithIdThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable command) {
            Thread thread = new Thread(command);
            thread.setName(this.namePrefix + "-" + this.threadId.getAndAdd(1));
            return thread;
        }
    }

}
