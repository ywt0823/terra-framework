package com.terra.framework.common.util.result;


import com.terra.framework.common.result.Result;
import com.terra.framework.common.result.ResultEnum;
import com.terra.framework.common.result.ResultPageInfo;

/**
 * @author ywt
 * @date 2020年2月9日 08:58:39
 **/
public class ResultUtils {
    /**
     * 返回成功，传入返回体具体出參
     *
     * @param t
     * @return
     */
    public static <T> Result<T> success(final T t) {
        return success(t, null);
    }

    public static <T> Result<T> success(final T t, final ResultPageInfo resultPageInfo) {
        return Result.<T>builder()
                .status(ResultEnum.SUCCESS)
                .statusCode(ResultEnum.SUCCESS.getCode())
                .data(t)
                .resultPageInfo(resultPageInfo)
                .build();
    }

    public static <T> Result<T> success(final T t, final String message, final ResultPageInfo resultPageInfo) {
        return Result.<T>builder()
                .status(ResultEnum.SUCCESS)
                .statusCode(ResultEnum.SUCCESS.getCode())
                .data(t)
                .msg(message)
                .resultPageInfo(resultPageInfo)
                .build();
    }


    /**
     * 提供给部分不需要出參的接口
     *
     * @return
     */
    public static Result success() {
        return success(null, null);
    }

    /**
     * 自定义错误信息
     *
     * @param status
     * @param msg
     * @return
     */
    public static Result error(final ResultEnum status, final String msg) {
        Result error = error(status);
        error.setMsg(msg);
        return error;
    }

    public static Result error(final ResultEnum status) {
        return Result.<String>builder()
                .status(status)
                .statusCode(status.getCode())
                .data(null)
                .msg(status.getMessage())
                .resultPageInfo(null)
                .build();
    }


}
