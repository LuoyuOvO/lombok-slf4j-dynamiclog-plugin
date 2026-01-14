package com.luoyu.dynamiclog.agent;

import com.luoyu.dynamiclog.trace.TraceContext;
import com.luoyu.dynamiclog.trace.TraceManager;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 方法拦截器
 *
 * @author system
 * @date 2024-01-01
 */
public class MethodInterceptor {
    private static final TraceManager traceManager = new TraceManager();
    private static final ThreadLocal<TraceContext> traceContextThreadLocal = new ThreadLocal<>();

    /**
     * 拦截方法调用
     *
     * @param method 方法
     * @param callable 原始调用
     * @return 方法返回值
     * @throws Exception 异常
     */
    @RuntimeType
    public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable) throws Exception {
        // 获取当前线程的TraceContext
        TraceContext context = traceContextThreadLocal.get();
        
        if (context == null) {
            // 如果没有上下文，直接执行
            return callable.call();
        }

        // 获取类名和方法名
        String className = method.getDeclaringClass().getName();
        String methodName = method.getName();
        
        // 获取行号（通过堆栈跟踪）
        int lineNumber = getLineNumber(method);

        // 进入方法
        com.luoyu.dynamiclog.trace.TraceNode node = traceManager.enterMethod(context, className, methodName, lineNumber);

        try {
            // 执行方法
            Object result = callable.call();
            return result;
        } finally {
            // 退出方法
            if (node != null) {
                traceManager.exitMethod(context, node);
            }
        }
    }

    /**
     * 设置追踪上下文
     *
     * @param context 追踪上下文
     */
    public static void setTraceContext(TraceContext context) {
        traceContextThreadLocal.set(context);
    }

    /**
     * 清除追踪上下文
     */
    public static void clearTraceContext() {
        traceContextThreadLocal.remove();
    }

    /**
     * 获取行号（简化实现）
     *
     * @param method 方法
     * @return 行号
     */
    private static int getLineNumber(Method method) {
        // 通过堆栈跟踪获取行号
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            if (element.getClassName().equals(method.getDeclaringClass().getName()) &&
                element.getMethodName().equals(method.getName())) {
                return element.getLineNumber();
            }
        }
        return -1;
    }
}
