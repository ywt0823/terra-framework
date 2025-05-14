package com.terra.framework.crust.customizer;

import com.google.common.collect.Maps;
import com.terra.framework.crust.model.Header;

import java.util.Map;

/**
 * ZiroomGateway header 信息
 *
 * @author Shawn
 * @version 1.0
 * @since 2020/11/17 15:30
 **/
public class RequestContextHolder {

    private static final ThreadLocal<Map<String, Object>> holder = ThreadLocal
            .withInitial(Maps::newHashMap);

    public static void set(String key, Header header) {
        holder.get().put(key, header);
    }

    public static <T extends Header> T get(String key, Class<T> type) {
        return type.cast(holder.get().get(key));
    }

    public static void remove() {
        holder.get().clear();
        holder.remove();
    }

    public static void remove(String key) {
        holder.get().remove(key);
    }
}
