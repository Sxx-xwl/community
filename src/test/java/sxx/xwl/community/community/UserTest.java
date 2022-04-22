package sxx.xwl.community.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import sxx.xwl.community.community.CommunityApplication;
import sxx.xwl.community.community.dao.UserMapper;
import sxx.xwl.community.community.entity.User;

import java.util.Date;

/**
 * @author sxx_27
 * @create 2022-04-20 16:29
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class UserTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(150);
        System.out.println(user);
        User user1 = userMapper.selectByEmail("nowcoder119@sina.com");
        System.out.println(user1);
        User jjj = userMapper.selectByName("jjj");
        System.out.println(jjj);
    }

    @Test
    public void testInsertUser() {
        User user = new User("test", "123456", "123", "1232131233@qq.com", 0, 0, null, "http://images.nowcoder.com/head/123.png", new Date());
        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser(){
        int i = userMapper.updateStatus(150, 1);
        System.out.println(i);

        int i1 = userMapper.updateHeader(150, "http://images.nowcoder.com/head/321.png");
        System.out.println(i1);

        int i2 = userMapper.updatePassword(150, "321654");
        System.out.println(i2);
    }

}
