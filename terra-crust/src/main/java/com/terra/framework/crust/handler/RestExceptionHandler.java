package com.terra.framework.crust.handler;

import com.terra.framework.common.result.Result;
import com.terra.framework.common.result.ResultEnum;
import com.terra.framework.common.util.result.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author ywt
 * @description
 * @date 2021年08月13日 14:01
 */
@RestControllerAdvice
@Configuration
@Slf4j
public class RestExceptionHandler {


    /**
     * 默认全局异常处理。
     *
     * @param e the e
     * @return ResultData
     */
    @ExceptionHandler(Exception.class)
    public Result<String> exception(Exception e) {
        log.error("全局异常信息 ex={}", e.getMessage(), e);
        return ResultUtils.error(ResultEnum.FAIL, e.getMessage());
    }
}
