package com.luoyu.dynamiclog.nacos;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luoyu.dynamiclog.config.DynamicLogConfig;
import com.luoyu.dynamiclog.config.InterfaceLogConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Nacos配置管理器
 *
 * @author system
 * @date 2024-01-01
 */
@Slf4j
public class NacosConfigManager {
    private ConfigService configService;
    private DynamicLogConfig dynamicLogConfig;
    private List<InterfaceLogConfig> interfaceConfigs = new ArrayList<>();
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 初始化Nacos配置服务
     *
     * @param config 动态日志配置
     * @throws NacosException Nacos异常
     */
    public void init(DynamicLogConfig config) throws NacosException {
        this.dynamicLogConfig = config;
        
        if (StringUtils.isBlank(config.getNacosServerAddr())) {
            log.warn("Nacos server address is blank, dynamic log plugin will not work");
            return;
        }

        // 创建Nacos配置服务
        configService = NacosFactory.createConfigService(config.getNacosServerAddr());

        // 初始化加载配置
        loadConfig();

        // 添加配置监听器
        addConfigListener();
    }

    /**
     * 加载配置
     */
    private void loadConfig() {
        try {
            String content = configService.getConfig(
                    dynamicLogConfig.getNacosDataId(),
                    dynamicLogConfig.getNacosGroup(),
                    5000
            );

            if (StringUtils.isNotBlank(content)) {
                parseConfig(content);
            }
        } catch (NacosException e) {
            log.error("Failed to load config from Nacos", e);
        }
    }

    /**
     * 解析配置内容
     *
     * @param content 配置内容（JSON格式）
     */
    private void parseConfig(String content) {
        try {
            NacosConfigData configData = objectMapper.readValue(content, NacosConfigData.class);
            
            if (configData.getEnabled() != null) {
                dynamicLogConfig.setEnabled(configData.getEnabled());
            }
            
            if (configData.getRemoteCallType() != null) {
                dynamicLogConfig.setRemoteCallType(configData.getRemoteCallType());
            }
            
            if (configData.getLogOutputPath() != null) {
                dynamicLogConfig.setLogOutputPath(configData.getLogOutputPath());
            }
            
            if (configData.getLogFileName() != null) {
                dynamicLogConfig.setLogFileName(configData.getLogFileName());
            }
            
            if (configData.getInterfaces() != null) {
                interfaceConfigs = configData.getInterfaces();
                // 重置所有接口的计数
                interfaceConfigs.forEach(ifc -> {
                    ifc.setCurrentCount(0);
                    ifc.setCompleted(false);
                });
            }
        } catch (Exception e) {
            log.error("Failed to parse config content", e);
        }
    }

    /**
     * 添加配置监听器
     */
    private void addConfigListener() {
        try {
            configService.addListener(
                    dynamicLogConfig.getNacosDataId(),
                    dynamicLogConfig.getNacosGroup(),
                    new Listener() {
                        @Override
                        public Executor getExecutor() {
                            return null;
                        }

                        @Override
                        public void receiveConfigInfo(String configInfo) {
                            log.info("Received config change from Nacos, reloading...");
                            parseConfig(configInfo);
                        }
                    }
            );
        } catch (NacosException e) {
            log.error("Failed to add config listener", e);
        }
    }

    /**
     * 获取接口配置
     *
     * @param path 接口路径
     * @param method HTTP方法
     * @return 接口配置，如果未匹配则返回null
     */
    public InterfaceLogConfig getInterfaceConfig(String path, String method) {
        if (!dynamicLogConfig.getEnabled()) {
            return null;
        }

        for (InterfaceLogConfig config : interfaceConfigs) {
            if (matches(config, path, method)) {
                return config;
            }
        }
        return null;
    }

    /**
     * 判断接口是否匹配
     *
     * @param config 配置
     * @param path 请求路径
     * @param method HTTP方法
     * @return 是否匹配
     */
    private boolean matches(InterfaceLogConfig config, String path, String method) {
        // 路径匹配（支持Ant风格）
        if (!matchPath(config.getPath(), path)) {
            return false;
        }

        // 方法匹配
        if (StringUtils.isNotBlank(config.getMethod())) {
            if (!config.getMethod().equalsIgnoreCase(method)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 路径匹配（简单Ant风格实现）
     *
     * @param pattern 模式
     * @param path 路径
     * @return 是否匹配
     */
    private boolean matchPath(String pattern, String path) {
        if (StringUtils.isBlank(pattern)) {
            return false;
        }

        // 完全匹配
        if (pattern.equals(path)) {
            return true;
        }

        // 通配符匹配
        pattern = pattern.replace(".", "\\.");
        pattern = pattern.replace("*", ".*");
        pattern = pattern.replace("?", ".?");

        return path.matches(pattern);
    }

    /**
     * 获取动态日志配置
     *
     * @return 动态日志配置
     */
    public DynamicLogConfig getDynamicLogConfig() {
        return dynamicLogConfig;
    }

    /**
     * 关闭配置服务
     */
    public void shutdown() {
        if (configService != null) {
            try {
                configService.shutDown();
            } catch (NacosException e) {
                log.error("Failed to shutdown Nacos config service", e);
            }
        }
    }

    /**
     * Nacos配置数据类
     */
    @lombok.Data
    private static class NacosConfigData {
        private Boolean enabled;
        private String remoteCallType;
        private String logOutputPath;
        private String logFileName;
        private List<InterfaceLogConfig> interfaces;
    }
}
