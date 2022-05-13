package sxx.xwl.community.community.aspect;

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

/**
 * @author sxx_27
 * @create 2022-05-07 21:59
 */
@Component
@Aspect
public class ServiceLogAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceLogAspect.class);

    //*           sxx.xwl.community.community.service.*.*(..))
    //所有返回值   路径                              * 所有类  * 所有方法 （..）所有参数
    @Pointcut("execution(* sxx.xwl.community.community.service.*.*(..))")
    public void pointcut() {

    }

    //方法执行前
    @Before("pointcut()")
    public void before(JoinPoint joinpoint) {
        //用户[1.2.3.4]，在[***]，访问了[sxx.xwl.community.community.service.*.xxx()]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null){
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        //获得用户ip
        String ip = request.getRemoteHost();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        //获取调用方法的类名 + 方法名
        String target = joinpoint.getSignature().getDeclaringTypeName() + "." + joinpoint.getSignature().getName();

        LOGGER.info(String.format("用户[%s]，在[%s]，访问了[%s]", ip, now, target));
        System.out.println("before");
    }

}
