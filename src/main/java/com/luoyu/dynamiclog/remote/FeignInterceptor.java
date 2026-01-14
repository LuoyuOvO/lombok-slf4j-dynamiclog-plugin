package com.luoyu.dynamiclog.remote;

import com.luoyu.dynamiclog.config.DynamicLogConfig;
import com.luoyu.dynamiclog.log.LoggerLevelManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Feign调用拦截器
 *
 * @author system
 * @date 2024-01-01
 */
@Slf4j
public class FeignInterceptor {
    private DynamicLogConfig config;

    public FeignInterceptor(DynamicLogConfig config) {
        this.config = config;
    }

    /**
     * 拦截Feign调用
     *
     * @param serviceName 服务名
     * @param methodName 方法名
     * @param logLevel 日志级别
     */
    public void intercept(String serviceName, String methodName, String logLevel) {
        if (!"feign".equalsIgnoreCase(config.getRemoteCallType())) {
            return;
        }

        if (StringUtils.isBlank(logLevel)) {
            return;
        }

        // 修改Feign相关的Logger级别
        String loggerName = "feign." + serviceName + "." + methodName;
        LoggerLevelManager.setLoggerLevel(loggerName, logLevel);

        // Feign使用feign.Logger，需要修改feign包的日志级别
        LoggerLevelManager.setLoggerLevel("feign", logLevel);
    }
}
