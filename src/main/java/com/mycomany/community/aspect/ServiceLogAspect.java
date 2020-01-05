package com.mycomany.community.aspect;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;


@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger logger= LoggerFactory.getLogger(ServiceLogAspect.class);

    @Pointcut("execution(* com.mycomany.community.services.*.*(..))")
    public void pointcut(){

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //user ip address , visit serive in date.
       ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();

       if(attributes == null){
           return;
       }
       HttpServletRequest request = attributes.getRequest();
       String ip = request.getRemoteHost();
       String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
       String target = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
       logger.info(String.format("User[%s], visited[%s] in [%s]", ip,target,now));

    }
}
