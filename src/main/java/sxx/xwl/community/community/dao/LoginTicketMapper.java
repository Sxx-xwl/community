package sxx.xwl.community.community.dao;

import org.apache.ibatis.annotations.*;
import sxx.xwl.community.community.entity.LoginTicket;

/**
 * 登录凭证
 *
 * @author sxx_27
 * @create 2022-04-23 20:42
 */
@Mapper
public interface LoginTicketMapper {

    //插入一个凭证
    @Insert({
            "insert into login_ticket (user_id, ticket, status, expired) values(#{userId},#{ticket},#{status},#{expired}) ",
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);


    //通过ticket查询
    @Select({
            "select id,user_id,ticket,status,expired from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectByticket(String ticket);


    //修改凭证状态
    @Update({
            "update login_ticket set status=#{status} where ticket=#{ticket}"
    })
    int updateStatus(String ticket, int status);

}
