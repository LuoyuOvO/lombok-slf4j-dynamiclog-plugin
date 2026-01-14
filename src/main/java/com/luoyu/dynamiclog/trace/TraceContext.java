package com.luoyu.dynamiclog.trace;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * 追踪上下文
 *
 * @author system
 * @date 2024-01-01
 */
@Data
public class TraceContext {
    /**
     * 请求路径
     */
    private String path;

    /**
     * HTTP方法
     */
    private String method;

    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 结束时间
     */
    private long endTime;

    /**
     * 调用栈
     */
    private Stack<TraceNode> callStack = new Stack<>();

    /**
     * 所有节点
     */
    private List<TraceNode> allNodes = new ArrayList<>();

    /**
     * 根节点
     */
    private TraceNode rootNode;
}
