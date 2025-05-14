package com.terra.framework.bedrock.trace;

public class LoggingContextHolder {

    private static final ThreadLocal<LoggingContext> holder = ThreadLocal.withInitial(LoggingContext::new);

    public static LoggingContext get() {
        return holder.get();
    }

    public static void remove() {
        holder.remove();
    }

}
