package com.luoyu.dynamiclog.trace;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 追踪管理器
 *
 * @author system
 * @date 2024-01-01
 */
@Slf4j
public class TraceManager {

    /**
     * 开始追踪
     *
     * @param path 请求路径
     * @param method HTTP方法
     * @return 追踪上下文
     */
    public TraceContext startTrace(String path, String method) {
        TraceContext context = new TraceContext();
        context.setPath(path);
        context.setMethod(method);
        context.setStartTime(System.currentTimeMillis());

        TraceNode rootNode = new TraceNode();
        rootNode.setClassName("HTTP");
        rootNode.setMethodName(method + " " + path);
        rootNode.setStartTime(context.getStartTime());
        rootNode.setDepth(0);
        context.setRootNode(rootNode);
        context.getCallStack().push(rootNode);
        context.getAllNodes().add(rootNode);

        return context;
    }

    /**
     * 结束追踪
     *
     * @param context 追踪上下文
     */
    public void endTrace(TraceContext context) {
        if (context == null) {
            return;
        }

        context.setEndTime(System.currentTimeMillis());
        long totalCost = context.getEndTime() - context.getStartTime();

        // 完成所有未完成的节点
        while (!context.getCallStack().isEmpty()) {
            TraceNode node = context.getCallStack().pop();
            if (!node.isCompleted()) {
                node.setEndTime(context.getEndTime());
                node.setCost(node.getEndTime() - node.getStartTime());
                node.setCompleted(true);
            }
        }

        // 打印追踪结果
        printTraceResult(context, totalCost);
    }

    /**
     * 打印追踪结果
     *
     * @param context 追踪上下文
     * @param totalCost 总耗时
     */
    private void printTraceResult(TraceContext context, long totalCost) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========== 动态日志追踪结果 ==========\n");
        sb.append("接口: ").append(context.getMethod()).append(" ").append(context.getPath()).append("\n");
        sb.append("总耗时: ").append(totalCost).append("ms\n");
        sb.append("----------------------------------------\n");

        printNode(sb, context.getRootNode(), 0);

        sb.append("========================================\n");
        log.info(sb.toString());
    }

    /**
     * 打印节点
     *
     * @param sb StringBuilder
     * @param node 节点
     * @param indent 缩进级别
     */
    private void printNode(StringBuilder sb, TraceNode node, int indent) {
        if (node == null) {
            return;
        }

        // 缩进
        for (int i = 0; i < indent; i++) {
            sb.append("  ");
        }

        // 打印节点信息
        sb.append("`---");
        if (node.getLineNumber() != null) {
            sb.append("[").append(node.getLineNumber()).append("] ");
        }
        sb.append(node.getClassName()).append(".").append(node.getMethodName());
        sb.append(" (").append(node.getCost()).append("ms)");

        if (node.getCost() > 1000) {
            sb.append(" [慢方法]");
        }

        sb.append("\n");

        // 打印子节点
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            for (TraceNode child : node.getChildren()) {
                printNode(sb, child, indent + 1);
            }
        }
    }

    /**
     * 进入方法
     *
     * @param context 追踪上下文
     * @param className 类名
     * @param methodName 方法名
     * @param lineNumber 行号
     * @return 节点
     */
    public TraceNode enterMethod(TraceContext context, String className, String methodName, Integer lineNumber) {
        if (context == null || StringUtils.isBlank(className) || StringUtils.isBlank(methodName)) {
            return null;
        }

        TraceNode node = new TraceNode();
        node.setClassName(className);
        node.setMethodName(methodName);
        node.setLineNumber(lineNumber);
        node.setStartTime(System.currentTimeMillis());

        if (!context.getCallStack().isEmpty()) {
            TraceNode parent = context.getCallStack().peek();
            node.setParent(parent);
            node.setDepth(parent.getDepth() + 1);
            parent.getChildren().add(node);
        }

        context.getCallStack().push(node);
        context.getAllNodes().add(node);

        return node;
    }

    /**
     * 退出方法
     *
     * @param context 追踪上下文
     * @param node 节点
     */
    public void exitMethod(TraceContext context, TraceNode node) {
        if (context == null || node == null) {
            return;
        }

        if (!context.getCallStack().isEmpty() && context.getCallStack().peek() == node) {
            context.getCallStack().pop();
            node.setEndTime(System.currentTimeMillis());
            node.setCost(node.getEndTime() - node.getStartTime());
            node.setCompleted(true);
        }
    }
}
