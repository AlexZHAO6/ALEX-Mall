package com.alex.mallproduct.exception;

import com.alex.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.alex.mallproduct.controller")
@Slf4j
public class MallExceptionControllerAdvice {

    @ExceptionHandler(value = {MethodArgumentNotValidException.class})
    public R handleValidaException(MethodArgumentNotValidException e){
        log.error("data validation failed", e.getMessage(), e.getClass());
        BindingResult result = e.getBindingResult();

        Map<String, String> errors = new HashMap<>();
        result.getFieldErrors().forEach(item -> {
            String field = item.getField();
            String message = item.getDefaultMessage();
            errors.put(field, message);
        });

        return R.error(BizCodeEmum.VALID_EXCEPTION.getCode(), BizCodeEmum.VALID_EXCEPTION.getMsg()).put("data", errors);
    }

    @ExceptionHandler(value = {Throwable.class})
    public R handleException(Throwable e){

        return R.error(BizCodeEmum.UNKNOW_EXCEPTION.getCode(), BizCodeEmum.UNKNOW_EXCEPTION.getMsg());
    }

}
