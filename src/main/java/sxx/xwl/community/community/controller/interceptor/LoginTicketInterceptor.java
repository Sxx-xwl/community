package sxx.xwl.community.community.controller.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import sxx.xwl.community.community.entity.LoginTicket;
import sxx.xwl.community.community.entity.User;
import sxx.xwl.community.community.service.UserService;
import sxx.xwl.community.community.util.CookieUtil;
import sxx.xwl.community.community.util.HostHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 拦截器
 *
 * @author sxx_27
 * @create 2022-04-24 20:19
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    //controller执行之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null) {
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
                //通过凭证查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //将用户信息存入
                hostHolder.setUser(user);
                //构建用户认证的结果，并存入SecurityContext 以便于Security进行授权
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user, user.getPassword(), userService.getAuthorities(user.getId()));
                //为security设置权限
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        System.out.println("preLoginTicketInterceptor拦截器生效");
        return true;
    }

    //controller之后执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser", user);
        }
        System.out.println("postLoginTicketInterceptor拦截器生效");
    }

    //请求结束后执行
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        System.out.println("afterLoginTicketInterceptor拦截器生效");
    }
}
