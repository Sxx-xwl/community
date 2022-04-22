package sxx.xwl.community.community.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sxx.xwl.community.community.dao.UserMapper;
import sxx.xwl.community.community.entity.User;

/**
 * @author sxx_27
 * @create 2022-04-20 20:39
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public User findUserById(int userId){
        return userMapper.selectById(userId);
    }

}
