package sxx.xwl.community.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import sxx.xwl.community.community.util.CommunityConstant;
import sxx.xwl.community.community.util.CommunityUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author sxx_27
 * @create 2022-05-19 14:50
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        //忽略掉静态资源
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeRequests()
                //需要有权限才可以访问的路径
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/user/profile/**",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                //具备以下权限才可以访问
                .hasAnyAuthority(
                        AUTHORITY_ADMIN,
                        AUTHORITY_MODERATOR,
                        AUTHORITY_USER
                )
                //其他路径都可以访问
                .anyRequest().permitAll()
                .and().csrf().disable();

        //权限不够的处理方式
        http.exceptionHandling()
                //未登录的处理方式
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request,
                                         HttpServletResponse response,
                                         AuthenticationException authException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //异步请求 返回json
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "未登录！"));
                        }
                        //同步请求 返回页面
                        else {
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                //登录的但权限不够的处理方式
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request,
                                       HttpServletResponse response,
                                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //异步请求 返回json
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "没有访问权限！"));
                        }
                        //同步请求 返回页面
                        else {
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }
                    }
                });

        //security默认拦截logout请求，进行退出处理
        //覆盖默认逻辑，执行我们自己的代码
        http.logout().logoutUrl("/sxxlogout");
    }
}
