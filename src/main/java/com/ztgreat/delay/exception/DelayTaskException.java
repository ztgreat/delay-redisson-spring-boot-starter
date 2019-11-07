package com.ztgreat.delay.exception;

import java.io.Serializable;

/**
 * 任务 异常
 *
 * @author ztgreat
 */
public class DelayTaskException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = -1354043731046864103L;
    private String code;

    public DelayTaskException(String msg) {
        super(msg);
    }

}