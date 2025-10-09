package com.url.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

	private final static Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

//	pointcut - defines points in code where the advice will be called

	@Pointcut("execution (* com.url..*(..))  && !within(org.springframework.web.filter.GenericFilterBean+)")
	public void logAllMethods() {
	}

	@Before("logAllMethods()")
	public void logBeforeExecution(JoinPoint jp) {

		logger.info("Entered method - " + jp.getSignature() + "with arguments - " + Arrays.toString(jp.getArgs()));

	}

//	@AfterReturning(pointcut = "logAllMethods()", returning = "result")
//	public void logAfter(JoinPoint jp, Object result) {
//		logger.info("Exiting method : " + jp.getSignature().toShortString() + " Result : " + result);
//	}

//	@AfterThrowing(pointcut = "logAllMethods()", throwing = "ex")
//	public void logException(JoinPoint jp, Exception ex) {
//		logger.error("Exception in method ", jp.getSignature().toShortString(), ex.getMessage(), ex);
//	}
}
