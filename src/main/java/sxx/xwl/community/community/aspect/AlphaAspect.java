package sxx.xwl.community.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @author sxx_27
 * @create 2022-05-07 21:39
 */
//@Component
//@Aspect
public class AlphaAspect {

    //*           sxx.xwl.community.community.service.*.*(..))
    //所有返回值   路径                              * 所有类  * 所有方法 （..）所有参数
    @Pointcut("execution(* sxx.xwl.community.community.service.*.*(..))")
    public void pointcut(){

    }

    //方法执行前
    @Before("pointcut()")
    public void before(){

        System.out.println("before");
    }

    //方法执行后
    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    //运行时
    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }

    //抛异常时
    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }


    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        System.out.println("around  before");
        Object proceed = joinPoint.proceed();
        System.out.println("around  after");
        return proceed;
    }
}
