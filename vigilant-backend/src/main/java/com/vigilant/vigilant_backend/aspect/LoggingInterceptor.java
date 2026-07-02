package com.vigilant.vigilant_backend.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

@Slf4j
public class LoggingInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String className = invocation.getMethod().getDeclaringClass().getName();
        String methodName = invocation.getMethod().getName();
        
        log.info("Enter: {}.{}() with argument[s] = {}", className, methodName, invocation.getArguments());
        
        try {
            long start = System.currentTimeMillis();
            Object result = invocation.proceed();
            long elapsedTime = System.currentTimeMillis() - start;
            
            log.info("Exit: {}.{}() with result = {} (Execution Time: {} ms)", className, methodName, result, elapsedTime);
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Illegal argument: {} in {}.{}()", invocation.getArguments(), className, methodName);
            throw e;
        } catch (Exception e) {
            log.error("Exception {} in {}.{}() with cause = {}", e.getClass().getName(), className, methodName, e.getCause() != null ? e.getCause() : "NULL");
            throw e;
        }
    }
}
