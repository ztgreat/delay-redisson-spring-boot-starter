package com.ztgreat.delay.timer.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 时间槽
 *
 * @author zhangteng
 */
public class TimerTaskList implements Delayed {

    /**
     * 过期时间
     */
    private AtomicLong expiration = new AtomicLong(-1L);

    /**
     * 根节点
     */
    private MemoryDelayedTask root = new MemoryDelayedTask("root", -1L, null);


    /**
     * 设置过期时间
     */
    public boolean setExpiration(long expire) {
        if (expire == expiration.get()) {
            return true;
        }
        return expiration.getAndSet(expire) != expire;
    }

    /**
     * 获取过期时间
     */
    public long getExpiration() {
        return expiration.get();
    }

    /**
     * 新增任务
     *
     * @param memoryDelayedTask 延迟任务
     * @return boolean 添加是否成功
     */
    boolean addTask(MemoryDelayedTask memoryDelayedTask) {
        synchronized (this) {
            if (memoryDelayedTask.timerTaskList == null) {
                memoryDelayedTask.timerTaskList = this;
                MemoryDelayedTask tail = root.pre;
                memoryDelayedTask.next = root;
                memoryDelayedTask.pre = tail;
                tail.next = memoryDelayedTask;
                root.pre = memoryDelayedTask;
                return true;
            }
        }
        return false;
    }

    /**
     * 移除任务
     *
     * @param memoryDelayedTask 延迟任务
     * @return boolean 是否成功
     */
    private boolean removeTask(MemoryDelayedTask memoryDelayedTask) {
        synchronized (this) {
            if (memoryDelayedTask.timerTaskList.equals(this)) {
                memoryDelayedTask.next.pre = memoryDelayedTask.pre;
                memoryDelayedTask.pre.next = memoryDelayedTask.next;
                memoryDelayedTask.timerTaskList = null;
                memoryDelayedTask.next = null;
                memoryDelayedTask.pre = null;
                return true;
            }
        }
        return false;
    }

    /**
     * 移除任务
     *
     * @param taskId 任务id
     * @return boolean 是否成功
     */
    boolean removeTask(String taskId) {

        synchronized (this) {
            MemoryDelayedTask iterator = root.pre;
            while (iterator != null && iterator != root) {
                if (iterator.getId().equals(taskId)) {
                    return removeTask(iterator);
                }
                iterator = iterator.pre;
            }
            return false;
        }
    }

    /**
     * 判断 任务 是否 存在
     *
     * @param taskId 任务id
     * @return boolean  是否成功
     */
    public boolean hasTask(String taskId) {
        MemoryDelayedTask iterator = root.pre;
        while (iterator != null && iterator != root) {
            if (iterator.getId().equals(taskId)) {
                return true;
            }
            iterator = iterator.pre;
        }
        return false;
    }


    /**
     * 获取 任务
     *
     * @param taskId 任务id
     * @return TimerTask  返回该任务的 引用
     */
    public MemoryDelayedTask getTask(String taskId) {
        MemoryDelayedTask iterator = root.pre;
        while (iterator != null && iterator != root) {
            if (iterator.getId().equals(taskId)) {
                return iterator;
            }
            iterator = iterator.pre;
        }
        return null;
    }

    /**
     * 重新分配
     */
    void flush(Consumer<MemoryDelayedTask> flush) {
        MemoryDelayedTask memoryDelayedTask = root.next;
        while (!memoryDelayedTask.equals(root)) {
            this.removeTask(memoryDelayedTask);
            flush.accept(memoryDelayedTask);
            memoryDelayedTask = root.next;
        }
        expiration.set(-1L);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return Math.max(0, unit.convert(expiration.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
    }

    @Override
    public int compareTo(Delayed o) {
        if (o instanceof TimerTaskList) {
            return Long.compare(expiration.get(), ((TimerTaskList) o).expiration.get());
        }
        return 0;
    }

    public List<MemoryDelayedTask> getAllTimerTasks() {
        List<MemoryDelayedTask> memoryDelayedTasks = new ArrayList<>();
        MemoryDelayedTask iterator = root.pre;
        while (iterator != null && iterator != root) {
            memoryDelayedTasks.add(iterator);
            iterator = iterator.pre;
        }
        return memoryDelayedTasks;
    }
}
