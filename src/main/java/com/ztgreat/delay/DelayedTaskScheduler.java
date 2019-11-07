package com.ztgreat.delay;

import com.ztgreat.delay.timer.DelayedTask;

/**
 * 延迟任务 调度器
 *
 * @author ztgreat
 */
public interface DelayedTaskScheduler {


    /**
     * 开启延迟任务调度
     *
     * @return boolean
     */
    boolean start();


    /**
     * 关闭
     *
     * @return boolean
     */
    boolean stop();

    /**
     * 提交延迟任务
     *
     * @param delayedTask 延迟任务
     * @return boolean
     */
    boolean submitTask(DelayedTask delayedTask);

}
