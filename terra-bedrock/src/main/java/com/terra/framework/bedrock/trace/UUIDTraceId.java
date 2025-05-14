package com.terra.framework.bedrock.trace;

import java.util.UUID;

public class UUIDTraceId {

    public static String create() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
