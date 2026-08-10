package com.example.orchardcommon.exception;

import lombok.Getter;

/**
 * 业务异常：携带业务错误码，由全局异常处理器转换为对应业务码返回客户端
 */
@Getter
public class BizException extends RuntimeException {

    /** 业务错误码，默认1001 */
    private final int code;

    public BizException(String message) {
        super(message);
        this.code = 1001;
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
