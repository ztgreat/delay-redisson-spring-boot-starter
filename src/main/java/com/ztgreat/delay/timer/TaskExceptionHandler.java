package com.ztgreat.delay.timer;

import java.io.Serializable;

public interface TaskExceptionHandler extends Serializable {
    void handle(DelayedTask task, Throwable e);
}
