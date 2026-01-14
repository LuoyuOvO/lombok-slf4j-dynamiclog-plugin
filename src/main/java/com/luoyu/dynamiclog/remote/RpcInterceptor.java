package com.luoyu.dynamiclog.remote;

import com.luoyu.dynamiclog.config.DynamicLogConfig;
import com.luoyu.dynamiclog.log.LoggerLevelManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * RPC调用拦截器
 *
 * @author system
 * @date 2024-01-01
 */
@Slf4j
public class RpcInterceptor {
    private DynamicLogConfig config;

    public RpcInterceptor(DynamicLogConfig config) {
        this.config = config;
    }

    /**
     * 拦截RPC调用
     *
     * @param serviceName 服务名
     * @param methodName 方法名
     * @param logLevel 日志级别
     */
    public void intercept(String serviceName, String methodName, String logLevel) {
        if (!"rpc".equalsIgnoreCase(config.getRemoteCallType())) {
            return;
        }

        if (StringUtils.isBlank(logLevel)) {
            return;
        }

        // 修改RPC相关的Logger级别
        String loggerName = "rpc." + serviceName + "." + methodName;
        LoggerLevelManager.setLoggerLevel(loggerName, logLevel);
    }
}
