package com.terra.framework.crust.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.terra.framework.crust.web.IpUtil.verifyIp;


@Slf4j
public final class WebUtil {

    private WebUtil() {
    }

    public static boolean isRawRequest(HttpServletRequest httpReq) {
        String contentType = httpReq.getContentType();
        if (contentType == null) return false;
        if (contentType.contains("application/json")) return true;
        return contentType.contains("application/xml");
    }

    public static boolean isFileStream(HttpServletResponse httpResp) {
        String contentType = httpResp.getContentType();
        if (contentType == null) return false;
        return contentType.contains("application/octet-stream");
    }

    public static String getClientIp(HttpServletRequest request) {
        //1: get ip address from x-forwarded-for
        String ipAddress = request.getHeader("x-forwarded-for");
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > 15) { //"***.***.***.***".length() = 15
            String[] ipAddressArray = ipAddress.split(",");
            if (ipAddressArray.length > 0) {
                ipAddress = ipAddressArray[ipAddressArray.length - 1].trim();
            }
        }
        if (verifyIp(ipAddress)) {
            return ipAddress;
        }

        //2: get ip address from REMOTE_ADDR
        ipAddress = request.getHeader("REMOTE_ADDR");

        if (verifyIp(ipAddress)) {
            return ipAddress;
        }

        //3: get ip address from X-Real-IP
        ipAddress = request.getHeader("X-Real-IP");
        if (verifyIp(ipAddress)) {
            return ipAddress;
        }

        ipAddress = request.getRemoteAddr();
        if (ipAddress.equals("127.0.0.1")) {
            //根据网卡取本机配置的IP
            InetAddress inet = null;
            try {
                inet = InetAddress.getLocalHost();
                ipAddress = inet.getHostAddress();
            } catch (UnknownHostException e) {
                log.error(e.getMessage(), e);
            }
        }
        return ipAddress;
    }

    /// ///////////////////////////cookie相关////////////////////////////////////////////////////////////////////////////
    public static Cookie getCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie cookie = getCookie(request, cookieName);
        return cookie != null && !"null".equalsIgnoreCase(cookie.getValue()) ? cookie.getValue() : null;
    }

    public static void setCookie(HttpServletResponse response, String name, String value, long expire) {
        setCookie(response, name, value, "/", expire);
    }

    public static void setCookie(HttpServletResponse response, String name, String value, String path, long expire) {
        if (value == null) throw new IllegalArgumentException("cookie值不能为空");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy HH:mm:ss");
        response.setHeader("SET-COOKIE", name + "=" + value
                + ";Path=" + path + ";expires=" + dateFormat.format(new Date(expire)) + " GMT" +
                ";HttpOnly");
    }

    public static void addCookie(HttpServletResponse response, String name, String value, String domain, Integer expire) {
        addCookie(response, name, value, domain, "/", expire);
    }

    public static void addCookie(HttpServletResponse response, String name, String value, String domain, String path, Integer seconds) {
//        if(value == null) throw new IllegalArgumentException("cookie值不能为空");
        Cookie cookie = new Cookie(name, value);
        cookie.setDomain(domain);
        cookie.setMaxAge(seconds);
        cookie.setPath(path);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletResponse response, String name) {
        deleteCookie(response, name, "/");
    }

    public static void deleteCookie(HttpServletResponse response, String name, String path) {
        response.setHeader("SET-COOKIE", name + "=;Path=" + path + ";expires=0;HttpOnly");
    }

    //////////////////////////////cookie相关end/////////////////////////////////////////////////////////////////////////

    /// ///////////////////////////session相关////////////////////////////////////////////////////////////////////////////
    public static void setSessionAttribute(HttpSession session, String sessionKey, String sessionId) {
        session.setAttribute(sessionKey, sessionId);
    }

    public static void removeSessionAttribute(HttpSession session, String sessionKey) {
        session.removeAttribute(sessionKey);
    }

    /// ///////////////////////////session相关end/////////////////////////////////////////////////////////////////////////

    public static boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return requestedWith != null && "XMLHttpRequest".equals(requestedWith);
    }

    public static String getUrl(HttpServletRequest request) {
        String url = request.getScheme() + "://" + request.getServerName()
                + ":" + request.getServerPort()
                + request.getServletPath();
        if (request.getQueryString() != null) {
            url += "?" + request.getQueryString();
        }
        return url;
    }

    public static String urlEncode(String value, String encoding) {
        if (value == null) {
            return "";
        }
        try {
            String encoded = URLEncoder.encode(value, encoding);
            return encoded.replace("+", "%20").replace("*", "%2A")
                    .replace("~", "%7E").replace("/", "%2F");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    public static String urlDecode(String value, String encoding) {
        if (value == null || "".equals(value)) {
            return value;
        }
        try {
            return URLDecoder.decode(value, encoding);
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    public static String paramToQueryString(Map<String, String> params, String[] excludes, String charset) {

        if (params == null || params.isEmpty()) {
            return null;
        }

        StringBuilder paramString = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> p : params.entrySet()) {
            String key = p.getKey();
            String value = p.getValue();
            if (excludes != null) {
                boolean ignore = false;
                for (String k : excludes) {
                    if (key.equals(k)) {
                        ignore = true;
                        break;
                    }
                }
                if (ignore) continue;
            }

            if (!first) {
                paramString.append("&");
            }

            // Urlencode each request parameter
            paramString.append(urlEncode(key, charset));
            if (value != null) {
                paramString.append("=").append(urlEncode(value, charset));
            }

            first = false;
        }

        return paramString.toString();
    }

    public static String paramToQueryString(Map<String, String> params, String charset) {
        return paramToQueryString(params, null, charset);
    }

    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public static String getHost(String url) {
        try {
            URL url_ = new URL(url);
            return url_.getHost();
        } catch (Exception e) {
            log.error("解析{}失败", url, e);
        }
        return null;
    }

    public static String getQueryValue(String url, String parameter) {
        try {
            URL url_ = new URL(url);
            List<String> v = getParameters(url_.getQuery()).get(parameter);
            if (v == null) {
                v = getParameters(url_.getRef().substring(url_.getRef().indexOf("?") + 1)).get(parameter);
            }
            return v == null ? null : v.stream().collect(Collectors.joining(";"));
        } catch (Exception e) {
            log.error("解析url参数失败{}", url, e);
        }
        return null;
    }

    public static Map<String, List<String>> getParameters(String query) {
        Map<String, List<String>> parameters = new LinkedHashMap<>();

        if (query != null && !query.isEmpty()) {
            for (String pair : query.split("&")) {
                int equalIndex = pair.indexOf("=");
                String key = equalIndex == -1 ? pair : pair.substring(0, equalIndex);
                String value = equalIndex == -1 ? null : pair.substring(equalIndex + 1);

                key = URLDecoder.decode(key, StandardCharsets.UTF_8);

                if (value != null) {
                    value = URLDecoder.decode(value, StandardCharsets.UTF_8);
                }

                if (!parameters.containsKey(key)) {
                    parameters.put(key, new ArrayList<String>());
                }

                parameters.get(key).add(value);
            }
        }

        return parameters;
    }


    public static String getPathWithinApplication(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (StringUtils.startsWithIgnoreCase(requestUri, contextPath)) {
            String path = requestUri.substring(contextPath.length());
            return (StringUtils.isNotBlank(path) ? path : "/");
        } else {
            return requestUri;
        }
    }


    public static byte[] getReqFromParameterMap(HttpServletRequest request) {
        try {
            StringBuffer sbuf = new StringBuffer();
            Map<String, String[]> form = request.getParameterMap();
            for (Iterator<String> nameIterator = form.keySet().iterator();
                 nameIterator.hasNext(); ) {
                String name = nameIterator.next();
                List<String> values = Arrays.asList(form.get(name));
                for (Iterator<String> valueIterator = values.iterator();
                     valueIterator.hasNext(); ) {
                    String value = valueIterator.next();
                    sbuf.append(name).append('=');
                    if (value != null) {
                        sbuf.append(value);
                        if (valueIterator.hasNext()) {
                            sbuf.append('&');
                        }
                    }
                }
                if (nameIterator.hasNext()) {
                    sbuf.append('&');
                }
            }
            return sbuf.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("获取params参数失败");
        }
        return new byte[0];
    }

}
