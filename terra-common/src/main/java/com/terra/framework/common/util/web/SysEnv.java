package com.terra.framework.common.util.web;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 系统环境(本地环境：local，开发环境 dev)
 *
 * @author ywt
 * @version V2.0
 * @since 2020/11/09 19:11
 */
public abstract class SysEnv {

    private static final Logger logger = LoggerFactory.getLogger(SysEnv.class);

    private SysEnv() {
    }

    /**
     * 本地环境：local，开发环境 dev
     */
    public enum NAMESPACE {
        local, dev;
    }

    public static String APPLICATION_ENV_DEFAULT_KEY = "APPLICATION_ENV";

    public static String APPLICATION_POD_NAME = "HOSTNAME";

    public static String getEnv() {
        String env = System.getenv(APPLICATION_ENV_DEFAULT_KEY);
        return env == null ? "" : env;
    }

    public static String getHostName() {
        // Omega hostname
        String hostname = System.getenv(APPLICATION_POD_NAME);

        if (StringUtils.isNotBlank(hostname)) {
            return hostname;
        }
        // 手动获取 hostname
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Aries Boot SysEnv get hostname fail!");
        }
        return hostname == null ? "" : hostname;
    }
}
