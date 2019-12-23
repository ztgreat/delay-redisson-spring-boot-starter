package com.ztgreat.delay;

import com.ztgreat.delay.timer.DelayedTask;
import com.ztgreat.delay.timer.memory.MemoryTimeWheelTimer;
import org.springframework.stereotype.Component;

/**
 * 基于内存的
 * 延迟任务 调度器
 *
 * @author ztgreat
 */
@Component
public class MemoryDelayedTaskScheduler implements DelayedTaskScheduler {


    private MemoryTimeWheelTimer timer;

    @Override
    public boolean start() {
        timer.start();
        return true;
    }

    @Override
    public boolean stop() {
        return false;
    }

    /**
     * 提交延迟任务
     *
     * @param delayedTask 延迟任务
     */
    @Override
    public boolean submitTask(DelayedTask delayedTask) {
        timer.addTask(delayedTask);
        return true;
    }

}
