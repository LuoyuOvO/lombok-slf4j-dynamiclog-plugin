package com.luoyu.dynamiclog.interceptor;

import com.luoyu.dynamiclog.config.InterfaceLogConfig;
import com.luoyu.dynamiclog.log.LoggerLevelManager;
import com.luoyu.dynamiclog.nacos.NacosConfigManager;
import com.luoyu.dynamiclog.trace.TraceContext;
import com.luoyu.dynamiclog.trace.TraceManager;
import com.luoyu.dynamiclog.util.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 动态日志拦截器
 *
 * @author system
 * @date 2024-01-01
 */
@Slf4j
public class DynamicLogInterceptor implements HandlerInterceptor {
    private NacosConfigManager nacosConfigManager;
    private TraceManager traceManager;

    public DynamicLogInterceptor(NacosConfigManager nacosConfigManager, TraceManager traceManager) {
        this.nacosConfigManager = nacosConfigManager;
        this.traceManager = traceManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 获取接口配置
        InterfaceLogConfig interfaceConfig = nacosConfigManager.getInterfaceConfig(path, method);
        if (interfaceConfig == null) {
            return true;
        }

        // 检查是否已完成打印
        if (interfaceConfig.getCompleted()) {
            return true;
        }

        // 检查是否需要打印
        if (interfaceConfig.getCurrentCount() >= interfaceConfig.getCount()) {
            interfaceConfig.setCompleted(true);
            log.info("动态接口已经调试完毕，如果需要再生效请修改nacos配置，从新设置值。接口: {} {}", method, path);
            return true;
        }

        // 修改日志级别
        String logLevel = interfaceConfig.getLogLevel();
        if (StringUtils.isNotBlank(logLevel)) {
            // 修改当前接口相关的Logger级别
            String packageName = getPackageFromPath(path);
            if (StringUtils.isNotBlank(packageName)) {
                LoggerLevelManager.setLoggerLevelsForPackage(packageName, logLevel);
            }

            // 修改根Logger级别（可选，根据需求调整）
            LoggerLevelManager.setLoggerLevel(org.slf4j.Logger.ROOT_LOGGER_NAME, logLevel);

            log.info("匹配成功后的接口，打印记录日志。接口: {} {}, 日志级别: {}", method, path, logLevel);
        }

        // 设置请求到ThreadLocal供Aspect使用
        ThreadLocalUtil.setRequest(request);

        // 开始追踪
        TraceContext traceContext = traceManager.startTrace(path, method);
        request.setAttribute("_traceContext", traceContext);
        
        // 设置到ThreadLocal供Agent使用
        com.luoyu.dynamiclog.agent.MethodInterceptor.setTraceContext(traceContext);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TraceContext traceContext = (TraceContext) request.getAttribute("_traceContext");
        if (traceContext != null) {
            try {
                // 结束追踪并打印结果
                traceManager.endTrace(traceContext);
                
                // 增加计数
                String path = request.getRequestURI();
                String method = request.getMethod();
                InterfaceLogConfig interfaceConfig = nacosConfigManager.getInterfaceConfig(path, method);
                if (interfaceConfig != null && !interfaceConfig.getCompleted()) {
                    interfaceConfig.setCurrentCount(interfaceConfig.getCurrentCount() + 1);
                    
                    // 检查是否完成
                    if (interfaceConfig.getCurrentCount() >= interfaceConfig.getCount()) {
                        interfaceConfig.setCompleted(true);
                        log.info("动态接口已经调试完毕，如果需要再生效请修改nacos配置，从新设置值。接口: {} {}", method, path);
                        
                        // 恢复日志级别
                        String packageName = getPackageFromPath(path);
                        if (StringUtils.isNotBlank(packageName)) {
                            LoggerLevelManager.setLoggerLevelsForPackage(packageName, "INFO");
                        }
                        LoggerLevelManager.restoreLoggerLevel(org.slf4j.Logger.ROOT_LOGGER_NAME);
                    }
                }
            } finally {
                // 清除ThreadLocal
                com.luoyu.dynamiclog.agent.MethodInterceptor.clearTraceContext();
                ThreadLocalUtil.clearRequest();
            }
        }
    }

    /**
     * 从路径获取包名（简单实现，实际可能需要更复杂的逻辑）
     *
     * @param path 请求路径
     * @return 包名
     */
    private String getPackageFromPath(String path) {
        // 简单实现：从路径推断包名
        // 实际项目中可能需要配置映射关系
        if (StringUtils.isBlank(path)) {
            return null;
        }

        // 移除开头的斜杠
        path = path.startsWith("/") ? path.substring(1) : path;
        
        // 分割路径
        String[] parts = path.split("/");
        if (parts.length > 0) {
            // 假设第一段是模块名
            return "com.luoyu." + parts[0];
        }

        return null;
    }
}
