package com.luoyu.dynamiclog.config;

import lombok.Data;

/**
 * 接口日志配置
 *
 * @author system
 * @date 2024-01-01
 */
@Data
public class InterfaceLogConfig {
    /**
     * 接口路径（支持Ant风格匹配）
     */
    private String path;

    /**
     * HTTP方法（GET, POST, PUT, DELETE等，为空则匹配所有方法）
     */
    private String method;

    /**
     * 动态日志级别（TRACE, DEBUG, INFO, WARN, ERROR）
     */
    private String logLevel;

    /**
     * 需要打印的次数
     */
    private Integer count = 1;

    /**
     * 当前已打印次数
     */
    private Integer currentCount = 0;

    /**
     * 是否已打印完成
     */
    private Boolean completed = false;
}
