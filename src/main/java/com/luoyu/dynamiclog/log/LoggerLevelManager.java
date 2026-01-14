package com.luoyu.dynamiclog.log;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 日志级别管理器
 *
 * @author system
 * @date 2024-01-01
 */
@Slf4j
public class LoggerLevelManager {
    private static final Map<String, String> originalLevels = new ConcurrentHashMap<>();
    private static final Map<String, String> modifiedLevels = new ConcurrentHashMap<>();

    /**
     * 修改Logger的日志级别
     *
     * @param loggerName Logger名称
     * @param level 新的日志级别
     */
    public static void setLoggerLevel(String loggerName, String level) {
        if (StringUtils.isBlank(loggerName) || StringUtils.isBlank(level)) {
            return;
        }

        try {
            Logger logger = LoggerFactory.getLogger(loggerName);
            
            // 保存原始级别
            if (!originalLevels.containsKey(loggerName)) {
                String originalLevel = getLoggerLevel(logger);
                originalLevels.put(loggerName, originalLevel);
            }

            // 修改级别
            ch.qos.logback.classic.Logger logbackLogger = getLogbackLogger(logger);
            if (logbackLogger != null) {
                ch.qos.logback.classic.Level logbackLevel = convertToLogbackLevel(level);
                if (logbackLevel != null) {
                    logbackLogger.setLevel(logbackLevel);
                    modifiedLevels.put(loggerName, level);
                    log.debug("Set logger [{}] level to [{}]", loggerName, level);
                }
            } else {
                // 尝试使用Log4j2（通过反射，避免直接依赖）
                try {
                    Object log4jLogger = getLog4jLogger(logger);
                    if (log4jLogger != null) {
                        org.apache.logging.log4j.Level log4jLevel = convertToLog4jLevel(level);
                        if (log4jLevel != null) {
                            // 使用反射获取核心Logger
                            java.lang.reflect.Method getMessageLoggerMethod = 
                                log4jLogger.getClass().getMethod("getMessageLogger");
                            Object coreLogger = getMessageLoggerMethod.invoke(log4jLogger);
                            if (coreLogger != null && coreLogger instanceof org.apache.logging.log4j.core.Logger) {
                                java.lang.reflect.Method setLevelMethod = 
                                    coreLogger.getClass().getMethod("setLevel", org.apache.logging.log4j.Level.class);
                                setLevelMethod.invoke(coreLogger, log4jLevel);
                                modifiedLevels.put(loggerName, level);
                                log.debug("Set logger [{}] level to [{}]", loggerName, level);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Log4j2不可用，忽略
                    log.debug("Log4j2 logger not available for [{}]", loggerName);
                }
            }
        } catch (Exception e) {
            log.error("Failed to set logger level for [{}]", loggerName, e);
        }
    }

    /**
     * 恢复Logger的原始日志级别
     *
     * @param loggerName Logger名称
     */
    public static void restoreLoggerLevel(String loggerName) {
        if (StringUtils.isBlank(loggerName)) {
            return;
        }

        String originalLevel = originalLevels.get(loggerName);
        if (originalLevel == null) {
            return;
        }

        try {
            Logger logger = LoggerFactory.getLogger(loggerName);
            ch.qos.logback.classic.Logger logbackLogger = getLogbackLogger(logger);
            if (logbackLogger != null) {
                ch.qos.logback.classic.Level logbackLevel = convertToLogbackLevel(originalLevel);
                if (logbackLevel != null) {
                    logbackLogger.setLevel(logbackLevel);
                    modifiedLevels.remove(loggerName);
                    log.debug("Restored logger [{}] level to [{}]", loggerName, originalLevel);
                }
            } else {
                // 尝试使用Log4j2（通过反射，避免直接依赖）
                try {
                    Object log4jLogger = getLog4jLogger(logger);
                    if (log4jLogger != null) {
                        org.apache.logging.log4j.Level log4jLevel = convertToLog4jLevel(originalLevel);
                        if (log4jLevel != null) {
                            // 使用反射获取核心Logger
                            java.lang.reflect.Method getMessageLoggerMethod = 
                                log4jLogger.getClass().getMethod("getMessageLogger");
                            Object coreLogger = getMessageLoggerMethod.invoke(log4jLogger);
                            if (coreLogger != null && coreLogger instanceof org.apache.logging.log4j.core.Logger) {
                                java.lang.reflect.Method setLevelMethod = 
                                    coreLogger.getClass().getMethod("setLevel", org.apache.logging.log4j.Level.class);
                                setLevelMethod.invoke(coreLogger, log4jLevel);
                                modifiedLevels.remove(loggerName);
                                log.debug("Restored logger [{}] level to [{}]", loggerName, originalLevel);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Log4j2不可用，忽略
                    log.debug("Log4j2 logger not available for [{}]", loggerName);
                }
            }
        } catch (Exception e) {
            log.error("Failed to restore logger level for [{}]", loggerName, e);
        }
    }

    /**
     * 批量修改Logger级别
     *
     * @param loggerNames Logger名称列表
     * @param level 日志级别
     */
    public static void setLoggerLevels(String[] loggerNames, String level) {
        if (loggerNames == null || loggerNames.length == 0) {
            return;
        }

        for (String loggerName : loggerNames) {
            setLoggerLevel(loggerName, level);
        }
    }

    /**
     * 批量恢复Logger级别
     *
     * @param loggerNames Logger名称列表
     */
    public static void setLoggerLevelsForPackage(String packageName, String level) {
        if (StringUtils.isBlank(packageName)) {
            return;
        }

        // 获取包下所有Logger并修改级别
        Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        ch.qos.logback.classic.Logger logbackLogger = getLogbackLogger(rootLogger);
        if (logbackLogger != null) {
            ch.qos.logback.classic.LoggerContext context = 
                (ch.qos.logback.classic.LoggerContext) LoggerFactory.getILoggerFactory();
            for (ch.qos.logback.classic.Logger logger : context.getLoggerList()) {
                if (logger.getName().startsWith(packageName)) {
                    ch.qos.logback.classic.Level logbackLevel = convertToLogbackLevel(level);
                    if (logbackLevel != null) {
                        logger.setLevel(logbackLevel);
                    }
                }
            }
        }
    }

    /**
     * 获取Logger的当前级别
     *
     * @param logger Logger实例
     * @return 日志级别字符串
     */
    private static String getLoggerLevel(Logger logger) {
        if (logger == null) {
            return "INFO";
        }

        ch.qos.logback.classic.Logger logbackLogger = getLogbackLogger(logger);
        if (logbackLogger != null && logbackLogger.getLevel() != null) {
            return logbackLogger.getLevel().toString();
        }

        return "INFO";
    }

    /**
     * 获取Logback Logger
     *
     * @param logger SLF4J Logger
     * @return Logback Logger
     */
    private static ch.qos.logback.classic.Logger getLogbackLogger(Logger logger) {
        if (logger instanceof ch.qos.logback.classic.Logger) {
            return (ch.qos.logback.classic.Logger) logger;
        }
        return null;
    }

    /**
     * 获取Log4j2 Logger（通过反射，避免直接依赖）
     *
     * @param logger SLF4J Logger
     * @return Log4j2 Logger
     */
    private static Object getLog4jLogger(Logger logger) {
        try {
            Class<?> log4jLoggerClass = Class.forName("org.apache.logging.slf4j.Log4jLogger");
            if (log4jLoggerClass.isInstance(logger)) {
                return logger;
            }
        } catch (ClassNotFoundException e) {
            // Log4j2不可用
        }
        return null;
    }

    /**
     * 转换为Logback级别
     *
     * @param level 级别字符串
     * @return Logback级别
     */
    private static ch.qos.logback.classic.Level convertToLogbackLevel(String level) {
        if (StringUtils.isBlank(level)) {
            return null;
        }

        String upperLevel = level.toUpperCase();
        switch (upperLevel) {
            case "TRACE":
                return ch.qos.logback.classic.Level.TRACE;
            case "DEBUG":
                return ch.qos.logback.classic.Level.DEBUG;
            case "INFO":
                return ch.qos.logback.classic.Level.INFO;
            case "WARN":
                return ch.qos.logback.classic.Level.WARN;
            case "ERROR":
                return ch.qos.logback.classic.Level.ERROR;
            default:
                return null;
        }
    }

    /**
     * 转换为Log4j2级别
     *
     * @param level 级别字符串
     * @return Log4j2级别
     */
    private static org.apache.logging.log4j.Level convertToLog4jLevel(String level) {
        if (StringUtils.isBlank(level)) {
            return null;
        }

        String upperLevel = level.toUpperCase();
        switch (upperLevel) {
            case "TRACE":
                return org.apache.logging.log4j.Level.TRACE;
            case "DEBUG":
                return org.apache.logging.log4j.Level.DEBUG;
            case "INFO":
                return org.apache.logging.log4j.Level.INFO;
            case "WARN":
                return org.apache.logging.log4j.Level.WARN;
            case "ERROR":
                return org.apache.logging.log4j.Level.ERROR;
            default:
                return null;
        }
    }

    /**
     * 清除所有修改的级别
     */
    public static void clearAll() {
        for (String loggerName : modifiedLevels.keySet()) {
            restoreLoggerLevel(loggerName);
        }
        originalLevels.clear();
        modifiedLevels.clear();
    }
}
