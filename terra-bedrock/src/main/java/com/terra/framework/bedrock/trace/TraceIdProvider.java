package com.terra.framework.bedrock.trace;

import org.springframework.beans.BeansException;

import java.util.function.Supplier;

public interface TraceIdProvider<T> {

    T getIfAvailable();

    default T getIfAvailable(Supplier<T> defaultSupplier) throws BeansException {
        T dependency = getIfAvailable();
        return (dependency != null ? dependency : defaultSupplier.get());
    }
}
