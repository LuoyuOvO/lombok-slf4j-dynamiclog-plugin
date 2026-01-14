package com.luoyu.dynamiclog.util;

import javax.servlet.http.HttpServletRequest;

/**
 * ThreadLocal工具类
 *
 * @author system
 * @date 2024-01-01
 */
public class ThreadLocalUtil {
    private static final ThreadLocal<HttpServletRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 设置当前请求
     *
     * @param request HTTP请求
     */
    public static void setRequest(HttpServletRequest request) {
        REQUEST_THREAD_LOCAL.set(request);
    }

    /**
     * 获取当前请求
     *
     * @return HTTP请求
     */
    public static HttpServletRequest getRequest() {
        return REQUEST_THREAD_LOCAL.get();
    }

    /**
     * 清除当前请求
     */
    public static void clearRequest() {
        REQUEST_THREAD_LOCAL.remove();
    }
}
