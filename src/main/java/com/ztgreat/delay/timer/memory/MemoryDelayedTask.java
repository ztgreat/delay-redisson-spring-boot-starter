package com.ztgreat.delay.timer.memory;

import com.ztgreat.delay.timer.DelayedTask;

/**
 * 任务
 *
 * @author zhangteng
 */
public class MemoryDelayedTask extends DelayedTask {

    /**
     * 任务名字
     */
    private String name;

    /**
     * 延迟时间
     */
    private long delayMs;

    /**
     * 延迟任务结束后 执行的任务
     */
    private Runnable task;

    /**
     * 这个 task 所属 时间槽（该时间槽 可能包含多个任务，结构形式 是链表）
     */
    protected TimerTaskList timerTaskList;

    /**
     * 下一个节点
     */
    protected MemoryDelayedTask next;

    /**
     * 上一个节点
     */
    protected MemoryDelayedTask pre;


    public MemoryDelayedTask(String name, long delayMs, Runnable task) {
        this.delayMs = delayMs;
        this.name = name;
        this.task = task;
        this.timerTaskList = null;
        this.next = this;
        this.pre = this;
    }

    @Override
    public void run() {

    }

    @Override
    public long getDelay() {
        return delayMs;
    }

    public String getName() {
        return name;
    }

    public Runnable getTask() {
        return task;
    }


    @Override
    public String toString() {
        return name;
    }
}
