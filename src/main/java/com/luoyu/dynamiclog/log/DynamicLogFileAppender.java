package com.luoyu.dynamiclog.log;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 动态日志文件追加器
 *
 * @author system
 * @date 2024-01-01
 */
@Slf4j
public class DynamicLogFileAppender {
    private String logOutputPath;
    private String logFileName;
    private PrintWriter writer;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 初始化
     *
     * @param logOutputPath 日志输出路径
     * @param logFileName 日志文件名
     */
    public void init(String logOutputPath, String logFileName) {
        this.logOutputPath = logOutputPath;
        this.logFileName = logFileName;

        if (StringUtils.isBlank(logOutputPath) || StringUtils.isBlank(logFileName)) {
            return;
        }

        try {
            File dir = new File(logOutputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File logFile = new File(dir, logFileName);
            writer = new PrintWriter(new FileWriter(logFile, true), true);
        } catch (IOException e) {
            log.error("Failed to initialize dynamic log file appender", e);
        }
    }

    /**
     * 追加日志
     *
     * @param level 日志级别
     * @param message 日志消息
     */
    public void append(String level, String message) {
        if (writer == null) {
            return;
        }

        String timestamp = dateFormat.format(new Date());
        String logLine = String.format("[%s] [%s] %s%n", timestamp, level, message);
        writer.print(logLine);
        writer.flush();
    }

    /**
     * 关闭
     */
    public void close() {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }
}
