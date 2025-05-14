package com.terra.framework.crust.web;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public final class IpUtil {

    private static final Logger logger = LoggerFactory.getLogger(IpUtil.class);
    private final static Pattern ipPattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");

    private IpUtil() {}

    public static boolean verifyIp(String ip) {
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip))
            return false;
        return ipPattern.matcher(ip).matches();
    }

    public static long[] segments(String ip) {
        long[] array = new long[4];
        int position1 = ip.indexOf(".");
        int position2 = ip.indexOf(".", position1 + 1);
        int position3 = ip.indexOf(".", position2 + 1);
        array[0] = Long.parseLong(ip.substring(0, position1));
        array[1] = Long.parseLong(ip.substring(position1 + 1, position2));
        array[2] = Long.parseLong(ip.substring(position2 + 1, position3));
        array[3] = Long.parseLong(ip.substring(position3 + 1));
        return array;
    }

    public static long ipToLong(String ip) {
        long[] array = segments(ip);
        return (array[0] << 24) + (array[1] << 16) + (array[2] << 8) + array[3];
    }

    public static String ipToString(long ip) {
        StringBuffer sb = new StringBuffer("");
        sb.append(String.valueOf((ip >>> 24)));
        sb.append(".");
        sb.append(String.valueOf((ip & 0x00FFFFFF) >>> 16));
        sb.append(".");
        sb.append(String.valueOf((ip & 0x0000FFFF) >>> 8));
        sb.append(".");
        sb.append(String.valueOf((ip & 0x000000FF)));
        return sb.toString();
    }

    public static String getLocalHostAddress() {
        try {
            String ip = InetAddress.getLocalHost().getHostAddress().toString();
            if(logger.isDebugEnabled()) {
                logger.debug("ip of local host:{}", ip);
            }
            return ip;
        } catch (UnknownHostException e) {
            logger.error("获取本机ip地址失败", e);
        }
        return null;
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if( ip.indexOf(",")!=-1 ){
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
