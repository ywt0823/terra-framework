package com.terra.framework.common.result;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ywt
 * @date 2019年2月9日 08:57:48
 **/
@Data
@AllArgsConstructor
public class ResultPageInfo implements Serializable {

    /**
     * 总记录数
     */
    public String totalRecord;
    /**
     * 当前页
     */
    public String currentPage;
    /**
     * 页面最大记录数
     */
    public String pageSize;
    /**
     * 总页数
     */
    public String totalPage;

}
