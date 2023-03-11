package com.h.common.config.exception;

import com.h.common.result.Result;
import com.h.common.result.ResultCodeEnum;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 同意异常处理
 * @author: Lin
 * @since: 2023-03-01
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception e){
        e.printStackTrace();
        return Result.fail().message("执行了全局异常处理....");
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Result error(ServiceException e){
        e.printStackTrace();
        return Result.fail().message("执行了ServiceException异常处理....");
    }


    @ExceptionHandler(ArithmeticException.class)
    @ResponseBody
    public Result error(ArithmeticException e){
        e.printStackTrace();
        return Result.fail().message("执行了ArithmeticException异常....");
    }

    /**
     * spring security异常
     * @param e
     * @return
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public Result error(AccessDeniedException e) throws AccessDeniedException {
        return Result.build(null, ResultCodeEnum.PERMISSION);
    }


}
