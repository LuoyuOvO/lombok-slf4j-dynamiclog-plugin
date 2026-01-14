package com.luoyu.dynamiclog.config;

import lombok.Data;

/**
 * 动态日志配置类
 *
 * @author system
 * @date 2024-01-01
 */
@Data
public class DynamicLogConfig {
    /**
     * 是否启用动态日志插件
     */
    private Boolean enabled = false;

    /**
     * Nacos服务器地址
     */
    private String nacosServerAddr;

    /**
     * Nacos命名空间
     */
    private String nacosNamespace;

    /**
     * Nacos配置DataId
     */
    private String nacosDataId = "luoyu-dynamic-log-config";

    /**
     * Nacos配置Group
     */
    private String nacosGroup = "DEFAULT_GROUP";

    /**
     * 远程调用类型：rpc 或 feign
     */
    private String remoteCallType;

    /**
     * 日志输出路径
     */
    private String logOutputPath;

    /**
     * 日志文件名
     */
    private String logFileName = "dynamic-log.log";
}
