package com.terra.framework.common.exception;

import com.terra.framework.common.result.ErrorType;
import lombok.*;

/**
 * @author ywt
 * @description 异常
 * @date 2022年11月28日 9:31
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public abstract class TerraBaseException extends RuntimeException {
    /**
     * 异常码
     */
    protected Integer code;
    /**
     * 异常信息
     */
    protected String message;


    protected TerraBaseException(ErrorType errorType) {
        this(errorType.getCode(), errorType.getMessage());
    }


}
