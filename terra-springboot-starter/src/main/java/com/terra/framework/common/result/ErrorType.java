package com.terra.framework.common.result;

/**
 * @author ywt
 * @description
 * @date 2022年11月28日 9:35
 */
public interface ErrorType {

    /**
     * 相应编码
     *
     * @return
     */
    int getCode();

    /**
     * 响应信息
     *
     * @return
     */
    String getMessage();
}
