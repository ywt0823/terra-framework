package com.terra.framework.bedrock.trace;

/**
 * 分布式追踪ID生成器接口.
 * <p>
 * 定义了生成唯一追踪ID的标准方法. 框架提供一个默认的UUID实现 ({@link UUIDTraceIdGenerator}),
 * 用户可以提供自己的实现并注册为Spring Bean, 以覆盖默认行为.
 * 这允许与外部追踪系统 (如 SkyWalking, Zipkin) 进行集成.
 * </p>
 *
 * @author Terra Framework Team
 */
public interface TraceIdGenerator {

    /**
     * 生成一个新的追踪ID.
     *
     * @return a {@link java.lang.String} object.
     */
    String generate();
} 