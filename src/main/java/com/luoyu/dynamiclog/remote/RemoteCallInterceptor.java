package com.luoyu.dynamiclog.remote;

import com.luoyu.dynamiclog.config.DynamicLogConfig;
import com.luoyu.dynamiclog.config.InterfaceLogConfig;
import com.luoyu.dynamiclog.log.LoggerLevelManager;
import com.luoyu.dynamiclog.nacos.NacosConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 远程调用拦截器（统一入口）
 *
 * @author system
 * @date 2024-01-01
 */
@Slf4j
public class RemoteCallInterceptor {
    private NacosConfigManager nacosConfigManager;
    private DynamicLogConfig dynamicLogConfig;
    private RpcInterceptor rpcInterceptor;
    private FeignInterceptor feignInterceptor;

    public RemoteCallInterceptor(NacosConfigManager nacosConfigManager, 
                                  DynamicLogConfig dynamicLogConfig,
                                  RpcInterceptor rpcInterceptor,
                                  FeignInterceptor feignInterceptor) {
        this.nacosConfigManager = nacosConfigManager;
        this.dynamicLogConfig = dynamicLogConfig;
        this.rpcInterceptor = rpcInterceptor;
        this.feignInterceptor = feignInterceptor;
    }

    /**
     * 拦截RPC调用
     *
     * @param serviceName 服务名
     * @param methodName 方法名
     * @param currentPath 当前请求路径
     * @param currentMethod 当前HTTP方法
     */
    public void interceptRpc(String serviceName, String methodName, String currentPath, String currentMethod) {
        // 检查当前请求是否匹配动态日志配置
        InterfaceLogConfig interfaceConfig = nacosConfigManager.getInterfaceConfig(currentPath, currentMethod);
        if (interfaceConfig == null || interfaceConfig.getCompleted()) {
            return;
        }

        String logLevel = interfaceConfig.getLogLevel();
        if (StringUtils.isNotBlank(logLevel)) {
            rpcInterceptor.intercept(serviceName, methodName, logLevel);
        }
    }

    /**
     * 拦截Feign调用
     *
     * @param serviceName 服务名
     * @param methodName 方法名
     * @param currentPath 当前请求路径
     * @param currentMethod 当前HTTP方法
     */
    public void interceptFeign(String serviceName, String methodName, String currentPath, String currentMethod) {
        // 检查当前请求是否匹配动态日志配置
        InterfaceLogConfig interfaceConfig = nacosConfigManager.getInterfaceConfig(currentPath, currentMethod);
        if (interfaceConfig == null || interfaceConfig.getCompleted()) {
            return;
        }

        String logLevel = interfaceConfig.getLogLevel();
        if (StringUtils.isNotBlank(logLevel)) {
            feignInterceptor.intercept(serviceName, methodName, logLevel);
        }
    }
}
