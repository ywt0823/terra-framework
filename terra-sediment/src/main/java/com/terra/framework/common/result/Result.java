package com.terra.framework.common.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ywt
 * @Date 2019年2月9日 08:59:01
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Result<T> {

    /**
     * 接口返回枚举
     */
    private ResultEnum status;
    /**
     * 接口返回状态code
     */
    private Integer statusCode;
    /**
     * 接口返回状态信息
     */
    private String msg;
    /**
     * 接口返回实体类
     */
    private T data;
    /**
     * 接口返回分页信息
     */
    private ResultPageInfo resultPageInfo;


    public void setStatus(ResultEnum resultEnum) {
        this.status = resultEnum;
        wrapResultCodeAndMessage(resultEnum);
    }

    /**
     * 封装参数返回参数
     *
     * @param resultEnum
     */
    private void wrapResultCodeAndMessage(ResultEnum resultEnum) {
        this.msg = resultEnum.getMessage();
        this.statusCode = resultEnum.getCode();
    }


}
