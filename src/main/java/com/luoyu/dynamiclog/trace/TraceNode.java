package com.luoyu.dynamiclog.trace;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 追踪节点
 *
 * @author system
 * @date 2024-01-01
 */
@Data
public class TraceNode {
    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 行号
     */
    private Integer lineNumber;

    /**
     * 开始时间
     */
    private long startTime;

    /**
     * 结束时间
     */
    private long endTime;

    /**
     * 耗时（毫秒）
     */
    private long cost;

    /**
     * 父节点
     */
    private TraceNode parent;

    /**
     * 子节点列表
     */
    private List<TraceNode> children = new ArrayList<>();

    /**
     * 深度
     */
    private int depth;

    /**
     * 是否已完成
     */
    private boolean completed = false;
}
