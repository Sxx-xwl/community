package sxx.xwl.community.community.dao;

import org.apache.ibatis.annotations.Mapper;
import sxx.xwl.community.community.entity.User;

/**
 * @author sxx_27
 * @create 2022-04-20 16:06
 */
@Mapper
public interface UserMapper {

    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(int id, int status);

    int updateHeader(int id, String headerUrl);

    int updatePassword(int id, String password);
}
