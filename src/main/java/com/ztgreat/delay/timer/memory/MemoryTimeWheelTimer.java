package com.ztgreat.delay.timer.memory;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.ztgreat.delay.timer.AbstractTimer;
import com.ztgreat.delay.timer.DelayedTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * 定时器
 *
 * @author zhangteng
 */
public class MemoryTimeWheelTimer extends AbstractTimer {


    private static Logger logger = LoggerFactory.getLogger(TimeWheel.class);

    /**
     * 底层时间轮
     */
    private TimeWheel timeWheel;

    /**
     * 一个Timer只有一个delayQueue
     */
    private DelayQueue<TimerTaskList> delayQueue;

    /**
     * 过期任务执行线程
     */
    private ExecutorService workerThreadPool;

    /**
     * 轮询delayQueue获取过期任务线程
     */
    private ExecutorService bossThreadPool;


    /**
     * 构造函数
     *
     * @param tickMs           初始 时间槽 范围
     * @param wheelSize        时间轮大小
     * @param checkTime        线程检查延迟任务间隔
     * @param workerThreadPool 用于处理 延迟任务的线程池
     */
    public MemoryTimeWheelTimer(long tickMs, int wheelSize, long checkTime, ExecutorService workerThreadPool) {
        timeWheel = new TimeWheel(tickMs, wheelSize, System.currentTimeMillis(), delayQueue);
        this.workerThreadPool = workerThreadPool;
        //checkTime ms获取一次过期任务
        bossThreadPool.submit(() -> {
            while (true) {
                this.advanceClock(checkTime);
            }
        });
    }

    /**
     * 添加任务
     */
    @Override
    protected synchronized void doAddTask(DelayedTask delayedTask) {

        if (!(delayedTask instanceof MemoryDelayedTask)) {
            logger.info("[延迟任务到期]-任务类型错误");
            return;
        }
        MemoryDelayedTask memoryDelayedTask = (MemoryDelayedTask) delayedTask;
        try {
            if (hasTask(memoryDelayedTask.getId())) {
                return;
            }
            //添加失败任务直接执行
            if (!timeWheel.addTask(memoryDelayedTask)) {
                logger.info("[延迟任务到期]-[{}],准备执行回调方法", memoryDelayedTask.getName());
                if (Objects.nonNull(memoryDelayedTask.getTask())) {
                    workerThreadPool.submit(memoryDelayedTask.getTask());
                }
            }
        } catch (Exception e) {
            logger.error("[延迟任务]-[{}],延迟任务添加/更新 失败", memoryDelayedTask.getName());
        }
    }

    /**
     * 移除任务
     * 根据任务id ,将任务从 延迟队列中移出去
     *
     * @param taskId 任务id
     */
    public synchronized void removeTask(String taskId) {
        Iterator<TimerTaskList> iterator = delayQueue.iterator();
        while (iterator.hasNext()) {
            TimerTaskList taskList = iterator.next();
            taskList.removeTask(taskId);
        }
    }

    /**
     * 判断 是否 存在 任务
     *
     * @param taskId 任务id
     * @return boolean 是否成功
     */
    public boolean hasTask(String taskId) {
        for (TimerTaskList taskList : delayQueue) {
            if (taskList.hasTask(taskId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取延迟任务 数量
     *
     * @return 数量
     */
    public int getTaskSize() {
        return delayQueue.size();
    }

    /**
     * 获取过期任务
     */
    private void advanceClock(long timeout) {
        TimerTaskList timerTaskList;
        try {
            timerTaskList = delayQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (timerTaskList != null) {
                //推进时间
                timeWheel.advanceClock(timerTaskList.getExpiration());
                //执行过期任务（包含降级操作）
                timerTaskList.flush(this::doAddTask);
            }
        } catch (Exception e) {
            logger.error("[延迟任务] 推进时间轮 进度 失败:[{}]", e);
        }
    }

    /**
     * 获取 任务
     *
     * @param taskId 任务id
     * @return TimerTask  返回该任务的 引用
     */
    public MemoryDelayedTask getTask(String taskId) {

        Iterator<TimerTaskList> iterator = delayQueue.iterator();
        while (iterator.hasNext()) {
            TimerTaskList taskList = iterator.next();
            if (taskList.hasTask(taskId)) {
                return taskList.getTask(taskId);
            }
        }
        return null;
    }

    @Override
    protected void onStart() {
        delayQueue = new DelayQueue<>();
        // 一个线程 去推进时间
        bossThreadPool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), ThreadFactoryBuilder.create().setNamePrefix("delay-queue-").build());
    }

    @Override
    protected void onStop() {

    }
}
