package com.terra.framework.nova.core;

/**
 * 模型供应商上下文持有者。
 * <p>
 * 使用 {@link ThreadLocal} 来存储当前线程需要调用的模型ID。
 * 这使得在服务层或控制器层设置模型ID，并在客户端代理中动态获取成为可能。
 *
 * @author <a href="mailto:love.yu@terra.com">Yu</a>
 * @version 1.0.0
 * @since 1.0.0
 */
public final class ModelProviderContextHolder {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    private ModelProviderContextHolder() {
    }

    /**
     * 设置当前线程的模型ID。
     *
     * @param modelId 模型的唯一标识符 (例如，Spring Bean 的名称)
     */
    public static void setModelId(String modelId) {
        CONTEXT_HOLDER.set(modelId);
    }

    /**
     * 获取当前线程的模型ID。
     *
     * @return 模型ID，如果未设置则返回 {@code null}
     */
    public static String getModelId() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前线程的上下文。
     * <p>
     * <b>重要:</b> 在操作完成后，必须调用此方法以防止内存泄漏。
     * 推荐在 finally 块中调用。
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }
} 