package com.vigilant.vigilant_backend.aspect;

import org.springframework.aop.Advisor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.JdkRegexpMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopConfig {

    @Bean
    public LoggingInterceptor loggingInterceptor() {
        return new LoggingInterceptor();
    }

    @Bean
    public Advisor loggingAdvisor() {
        // Using Spring's native regex pointcut to avoid any AspectJ dependency!
        JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut();
        pointcut.setPatterns(
            "com\\.vigilant\\.vigilant_backend\\.controller\\..*",
            "com\\.vigilant\\.vigilant_backend\\.service\\..*"
        );

        return new DefaultPointcutAdvisor(pointcut, loggingInterceptor());
    }
}
