package com.luoyu.dynamiclog.autoconfigure;

import com.luoyu.dynamiclog.aspect.FeignAspect;
import com.luoyu.dynamiclog.aspect.RpcAspect;
import com.luoyu.dynamiclog.config.DynamicLogConfig;
import com.luoyu.dynamiclog.interceptor.DynamicLogInterceptor;
import com.luoyu.dynamiclog.nacos.NacosConfigManager;
import com.luoyu.dynamiclog.remote.FeignInterceptor;
import com.luoyu.dynamiclog.remote.RemoteCallInterceptor;
import com.luoyu.dynamiclog.remote.RpcInterceptor;
import com.luoyu.dynamiclog.trace.TraceManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 动态日志自动配置类
 *
 * @author system
 * @date 2024-01-01
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "luoyu.dynamic-log", name = "enabled", havingValue = "true", matchIfMissing = false)
@EnableConfigurationProperties
public class DynamicLogAutoConfiguration implements WebMvcConfigurer, ApplicationListener<ApplicationReadyEvent> {

    @Value("${luoyu.dynamic-log.nacos-server-addr:}")
    private String nacosServerAddr;

    @Value("${luoyu.dynamic-log.nacos-namespace:}")
    private String nacosNamespace;

    @Value("${luoyu.dynamic-log.nacos-data-id:luoyu-dynamic-log-config}")
    private String nacosDataId;

    @Value("${luoyu.dynamic-log.nacos-group:DEFAULT_GROUP}")
    private String nacosGroup;

    @Value("${luoyu.dynamic-log.remote-call-type:}")
    private String remoteCallType;

    @Value("${luoyu.dynamic-log.log-output-path:}")
    private String logOutputPath;

    @Value("${luoyu.dynamic-log.log-file-name:dynamic-log.log}")
    private String logFileName;

    private NacosConfigManager nacosConfigManager;

    /**
     * 创建动态日志配置Bean
     *
     * @return 动态日志配置
     */
    @Bean
    @ConfigurationProperties(prefix = "luoyu.dynamic-log")
    public DynamicLogConfig dynamicLogConfig() {
        DynamicLogConfig config = new DynamicLogConfig();
        config.setNacosServerAddr(nacosServerAddr);
        config.setNacosNamespace(nacosNamespace);
        config.setNacosDataId(nacosDataId);
        config.setNacosGroup(nacosGroup);
        config.setRemoteCallType(remoteCallType);
        config.setLogOutputPath(logOutputPath);
        config.setLogFileName(logFileName);
        return config;
    }

    /**
     * 创建Nacos配置管理器
     *
     * @param config 动态日志配置
     * @return Nacos配置管理器
     */
    @Bean
    public NacosConfigManager nacosConfigManager(DynamicLogConfig config) {
        NacosConfigManager manager = new NacosConfigManager();
        try {
            manager.init(config);
            this.nacosConfigManager = manager;
        } catch (Exception e) {
            log.error("Failed to initialize Nacos config manager", e);
        }
        return manager;
    }

    /**
     * 创建追踪管理器
     *
     * @return 追踪管理器
     */
    @Bean
    public TraceManager traceManager() {
        return new TraceManager();
    }

    /**
     * 创建动态日志拦截器
     *
     * @param nacosConfigManager Nacos配置管理器
     * @param traceManager 追踪管理器
     * @return 动态日志拦截器
     */
    @Bean
    public DynamicLogInterceptor dynamicLogInterceptor(NacosConfigManager nacosConfigManager, TraceManager traceManager) {
        return new DynamicLogInterceptor(nacosConfigManager, traceManager);
    }

    /**
     * 创建RPC拦截器
     *
     * @param config 动态日志配置
     * @return RPC拦截器
     */
    @Bean
    public RpcInterceptor rpcInterceptor(DynamicLogConfig config) {
        return new RpcInterceptor(config);
    }

    /**
     * 创建Feign拦截器
     *
     * @param config 动态日志配置
     * @return Feign拦截器
     */
    @Bean
    public FeignInterceptor feignInterceptor(DynamicLogConfig config) {
        return new FeignInterceptor(config);
    }

    /**
     * 创建远程调用拦截器
     *
     * @param nacosConfigManager Nacos配置管理器
     * @param config 动态日志配置
     * @param rpcInterceptor RPC拦截器
     * @param feignInterceptor Feign拦截器
     * @return 远程调用拦截器
     */
    @Bean
    public RemoteCallInterceptor remoteCallInterceptor(NacosConfigManager nacosConfigManager,
                                                        DynamicLogConfig config,
                                                        RpcInterceptor rpcInterceptor,
                                                        FeignInterceptor feignInterceptor) {
        return new RemoteCallInterceptor(nacosConfigManager, config, rpcInterceptor, feignInterceptor);
    }

    /**
     * 创建Feign切面（仅在配置为feign时启用）
     *
     * @param remoteCallInterceptor 远程调用拦截器
     * @return Feign切面
     */
    @Bean
    @ConditionalOnProperty(prefix = "luoyu.dynamic-log", name = "remote-call-type", havingValue = "feign")
    public FeignAspect feignAspect(RemoteCallInterceptor remoteCallInterceptor) {
        return new FeignAspect();
    }

    /**
     * 创建RPC切面（仅在配置为rpc时启用）
     *
     * @param remoteCallInterceptor 远程调用拦截器
     * @return RPC切面
     */
    @Bean
    @ConditionalOnProperty(prefix = "luoyu.dynamic-log", name = "remote-call-type", havingValue = "rpc")
    public RpcAspect rpcAspect(RemoteCallInterceptor remoteCallInterceptor) {
        return new RpcAspect();
    }

    /**
     * 注册拦截器
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (nacosConfigManager != null) {
            TraceManager traceManager = traceManager();
            DynamicLogInterceptor interceptor = new DynamicLogInterceptor(nacosConfigManager, traceManager);
            registry.addInterceptor(interceptor)
                    .addPathPatterns("/**");
        }
    }

    /**
     * 应用启动完成后的处理
     *
     * @param event 应用就绪事件
     */
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (nacosConfigManager != null) {
            DynamicLogConfig config = nacosConfigManager.getDynamicLogConfig();
            if (config != null && config.getEnabled()) {
                log.info("加载动态日志插件成功，动态日志插件已启用");
            } else {
                log.info("加载动态日志插件成功，动态日志插件未启用");
            }
        } else {
            log.info("加载动态日志插件成功，动态日志插件未启用（Nacos配置管理器初始化失败）");
        }
    }
}
