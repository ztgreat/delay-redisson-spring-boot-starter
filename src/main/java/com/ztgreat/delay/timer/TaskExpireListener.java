package com.ztgreat.delay.timer;

/**
 * 任务 过期 监听
 *
 * @author ztgreat
 */
public interface TaskExpireListener {
    void process(String taskId);
}
