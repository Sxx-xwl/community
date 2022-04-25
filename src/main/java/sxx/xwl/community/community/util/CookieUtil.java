package sxx.xwl.community.community.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * @author sxx_27
 * @create 2022-04-24 20:56
 */
public class CookieUtil {

    public static String getValue(HttpServletRequest request,String name){
        if (request == null || name == null)
        {
            throw new IllegalArgumentException("参数为空！");
        }
        Cookie[] cookies = request.getCookies();
        if (cookies!=null)
        {
            for (Cookie cookie : cookies)
            {
                if (cookie.getName().equals(name))
                {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

}
