package sxx.xwl.community.community.util;

import org.springframework.stereotype.Component;
import sxx.xwl.community.community.entity.User;

/**持有用户信息（用于代替session对象）
 * @author sxx_27
 * @create 2022-04-24 21:15
 */

@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
