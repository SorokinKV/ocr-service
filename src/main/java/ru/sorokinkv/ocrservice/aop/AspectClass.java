package ru.sorokinkv.ocrservice.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;


/**
 * This is AspectClass.
 */

@Aspect
@Component
@Slf4j
public class AspectClass {

    /**
     * This is method to calculate execution time.
     *
     * @param proceedingJoinPoint
     * @return
     */
    @Around("@annotation(ru.sorokinkv.ocrservice.aop.annotations.LogExecutionTime)")
    public Object methodTimeLogger(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();

        // Get intercepted method details
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();

        // Measure method execution time
        StopWatch stopWatch = new StopWatch(className + "->" + methodName);
        stopWatch.start(methodName);
        Object result = proceedingJoinPoint.proceed();
        stopWatch.stop();
        // Log method execution time
        //        if (log.isInfoEnabled()) {
        //            logger.info(stopWatch.prettyPrint());
        log.info(stopWatch.prettyPrint());
        //        }
        return result;
    }
}
