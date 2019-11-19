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
    private TimerTask root = new TimerTask("root", "root", -1L, null);


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
     * @param timerTask 延迟任务
     * @return boolean 添加是否成功
     */
    boolean addTask(TimerTask timerTask) {
        synchronized (this) {
            if (timerTask.timerTaskList == null) {
                timerTask.timerTaskList = this;
                TimerTask tail = root.pre;
                timerTask.next = root;
                timerTask.pre = tail;
                tail.next = timerTask;
                root.pre = timerTask;
                return true;
            }
        }
        return false;
    }

    /**
     * 移除任务
     *
     * @param timerTask 延迟任务
     * @return boolean 是否成功
     */
    private boolean removeTask(TimerTask timerTask) {
        synchronized (this) {
            if (timerTask.timerTaskList.equals(this)) {
                timerTask.next.pre = timerTask.pre;
                timerTask.pre.next = timerTask.next;
                timerTask.timerTaskList = null;
                timerTask.next = null;
                timerTask.pre = null;
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
            TimerTask iterator = root.pre;
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
        TimerTask iterator = root.pre;
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
    public TimerTask getTask(String taskId) {
        TimerTask iterator = root.pre;
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
    void flush(Consumer<TimerTask> flush) {
        TimerTask timerTask = root.next;
        while (!timerTask.equals(root)) {
            this.removeTask(timerTask);
            flush.accept(timerTask);
            timerTask = root.next;
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

    public List<TimerTask> getAllTimerTasks() {
        List<TimerTask> timerTasks = new ArrayList<>();
        TimerTask iterator = root.pre;
        while (iterator != null && iterator != root) {
            timerTasks.add(iterator);
            iterator = iterator.pre;
        }
        return timerTasks;
    }
}
