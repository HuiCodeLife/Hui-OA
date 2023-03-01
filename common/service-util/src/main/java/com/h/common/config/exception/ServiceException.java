package com.h.common.config.exception;

import com.h.common.result.ResultCodeEnum;
import lombok.Data;
import lombok.ToString;

/**
 * @author: Lin
 * @since: 2023-03-01
 */
@Data
@ToString
public class ServiceException extends RuntimeException{

    private Integer code;

    private String message;

    /**
     * 通过状态码和错误消息创建异常对象
     * @param code
     * @param message
     */
    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 接收枚举类型对象
     * @param resultCodeEnum
     */
    public ServiceException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
        this.message = resultCodeEnum.getMessage();
    }
}
