   package com.webczw.my.netty.aop;

   import lombok.extern.slf4j.Slf4j;
   import org.aspectj.lang.ProceedingJoinPoint;
   import org.aspectj.lang.annotation.Around;
   import org.aspectj.lang.annotation.Aspect;
   import org.springframework.stereotype.Component;

   @Aspect
   @Component
   @Slf4j
   public class MethodLogPrintAspect {

       @Around("@annotation(com.webczw.my.netty.aop.MethodLogPrint)")
       public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
           long startTime = System.currentTimeMillis();
           log.info("方法开始执行: {}", joinPoint.getSignature().getName());
           
           try {
               Object result = joinPoint.proceed();
               
               long endTime = System.currentTimeMillis();
               log.info("方法执行结束: {}, 耗时: {}ms", 
                       joinPoint.getSignature().getName(), (endTime - startTime));
               return result;
           } catch (Exception e) {
               log.error("方法执行异常: {}", joinPoint.getSignature().getName(), e);
               throw e;
           }
       }
   }
   