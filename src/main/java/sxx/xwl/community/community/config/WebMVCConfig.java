package sxx.xwl.community.community.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sxx.xwl.community.community.controller.interceptor.LoginTicketInterceptor;

/**
 * @author sxx_27
 * @create 2022-04-24 20:45
 */
@Configuration
public class WebMVCConfig implements WebMvcConfigurer {

    @Autowired
    LoginTicketInterceptor loginTicketInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}
