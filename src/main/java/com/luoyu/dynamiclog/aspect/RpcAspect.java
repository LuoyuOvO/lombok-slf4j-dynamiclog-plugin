package com.luoyu.dynamiclog.aspect;

import com.luoyu.dynamiclog.remote.RemoteCallInterceptor;
import com.luoyu.dynamiclog.util.ThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * RPC调用切面
 *
 * @author system
 * @date 2024-01-01
 */
@Slf4j
@Aspect
@Component
public class RpcAspect {
    private RemoteCallInterceptor remoteCallInterceptor;

    @Autowired
    public void setRemoteCallInterceptor(RemoteCallInterceptor remoteCallInterceptor) {
        this.remoteCallInterceptor = remoteCallInterceptor;
    }

    /**
     * 拦截RPC调用（需要根据实际RPC框架调整切点表达式）
     *
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("@annotation(com.alibaba.dubbo.config.annotation.Reference) || " +
            "execution(* com.alibaba.dubbo.rpc.*.*(..)) || " +
            "execution(* org.apache.dubbo.rpc.*.*(..))")
    public Object interceptRpc(ProceedingJoinPoint joinPoint) throws Throwable {
        if (remoteCallInterceptor == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = ThreadLocalUtil.getRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        String path = request.getRequestURI();
        String method = request.getMethod();

        // 获取服务名和方法名
        String serviceName = getServiceName(joinPoint);
        String methodName = joinPoint.getSignature().getName();

        // 拦截远程调用
        remoteCallInterceptor.interceptRpc(serviceName, methodName, path, method);

        return joinPoint.proceed();
    }

    /**
     * 获取服务名
     *
     * @param joinPoint 连接点
     * @return 服务名
     */
    private String getServiceName(ProceedingJoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getName();
        // 从类名提取服务名
        if (className.contains(".")) {
            String[] parts = className.split("\\.");
            if (parts.length > 0) {
                return parts[parts.length - 1];
            }
        }
        return className;
    }
}
