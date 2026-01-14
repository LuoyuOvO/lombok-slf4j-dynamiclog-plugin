package com.luoyu.dynamiclog.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * 动态日志Java Agent
 *
 * @author system
 * @date 2024-01-01
 */
public class DynamicLogAgent {
    /**
     * Agent入口方法
     *
     * @param agentArgs Agent参数
     * @param inst Instrumentation实例
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("DynamicLogAgent started");

        new AgentBuilder.Default()
                .type(ElementMatchers.any())
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, ProtectionDomain protectionDomain) {
                        return builder.method(ElementMatchers.any())
                                .intercept(MethodDelegation.to(MethodInterceptor.class));
                    }
                })
                .installOn(inst);
    }

    /**
     * Agent入口方法（动态attach）
     *
     * @param agentArgs Agent参数
     * @param inst Instrumentation实例
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }
}
