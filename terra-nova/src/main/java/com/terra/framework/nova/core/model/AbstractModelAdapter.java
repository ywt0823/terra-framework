package com.terra.framework.nova.core.model;

import com.terra.framework.nova.core.exception.ErrorType;
import com.terra.framework.nova.core.exception.ModelException;
import java.net.SocketTimeoutException;
import lombok.extern.slf4j.Slf4j;

/**
 * 抽象模型适配器实现
 *
 * @author terra-nova
 */
@Slf4j
public abstract class AbstractModelAdapter implements ModelAdapter {

    /**
     * 请求参数映射策略
     */
    protected final RequestMappingStrategy requestMappingStrategy;

    /**
     * 认证提供者
     */
    protected final AuthProvider authProvider;

    /**
     * 构造函数
     *
     * @param requestMappingStrategy 请求参数映射策略
     * @param authProvider 认证提供者
     */
    protected AbstractModelAdapter(RequestMappingStrategy requestMappingStrategy, AuthProvider authProvider) {
        this.requestMappingStrategy = requestMappingStrategy;
        this.authProvider = authProvider;
    }

    @Override
    public ModelException handleException(Exception vendorException) {
        log.error("模型调用异常", vendorException);

        if (vendorException instanceof SocketTimeoutException) {
            return new ModelException("模型请求超时", vendorException, ErrorType.TIMEOUT_ERROR);
        }

        // 默认的通用异常处理
        return new ModelException("模型调用失败: " + vendorException.getMessage(),
                vendorException, ErrorType.UNKNOWN_ERROR);
    }
}
