package com.ztgreat.delay.timer.memory;

/**
 * 任务
 *
 * @author zhangteng
 */
public class TimerTask {


    /**
     * 任务id，唯一
     */
    private String id;

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
    protected TimerTask next;

    /**
     * 上一个节点
     */
    protected TimerTask pre;

    /**
     * 描述
     */
    public String desc;

    public TimerTask(String id, String name, long delayMs, Runnable task) {
        this.delayMs = System.currentTimeMillis() + delayMs;
        this.id = id;
        this.name = name;
        this.task = task;
        this.timerTaskList = null;
        this.next = this;
        this.pre = this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Runnable getTask() {
        return task;
    }

    public long getDelayMs() {
        return delayMs;
    }

    public Long getExpiration() {

        if (timerTaskList == null) {
            return null;
        }
        return timerTaskList.getExpiration();

    }

    @Override
    public String toString() {
        return name;
    }
}
