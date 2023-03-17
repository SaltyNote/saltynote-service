package com.saltynote.service.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ControllerMonitor {

    @Pointcut("execution(* com.saltynote.service.controller.*Controller.*(..))")
    public void monitor() {
        // an utility for aop
    }

    @Around("monitor()")
    public Object logServiceAccess(ProceedingJoinPoint pjp) throws Throwable {
        log.info("processing: " + pjp);
        long start = System.currentTimeMillis();
        Object output = pjp.proceed();
        long elapsedTime = System.currentTimeMillis() - start;
        log.info(pjp.getSignature() + " execution time: " + elapsedTime + " milliseconds.");
        return output;
    }
}
