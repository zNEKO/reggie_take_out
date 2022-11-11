package com.neko.reggie.common;

import com.neko.reggie.Exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@ResponseBody
@ControllerAdvice(annotations = {RestController.class, Controller.class})
public class GlobalExceptionHandler {

    /**
     * 捕获处理实体类字段重复异常
     * @param ex 捕获将要处理的异常
     * @return R 返回信息
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
//    java.sql.SQLIntegrityConstraintViolationException:
//    Duplicate entry 'zhangsan' for key 'idx_username'
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        log.info(ex.getMessage());

        if (ex.getMessage().contains("Duplicate entry")) {
            String[] s = ex.getMessage().split(" ");
            String msg = s[2] + "已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex) {
        log.info(ex.getMessage());

        return R.error(ex.getMessage());
    }

}
