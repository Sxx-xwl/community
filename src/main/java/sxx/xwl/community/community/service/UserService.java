package sxx.xwl.community.community.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sun.security.krb5.internal.Ticket;
import sxx.xwl.community.community.dao.LoginTicketMapper;
import sxx.xwl.community.community.dao.UserMapper;
import sxx.xwl.community.community.entity.LoginTicket;
import sxx.xwl.community.community.entity.User;
import sxx.xwl.community.community.util.CommunityConstant;
import sxx.xwl.community.community.util.CommunityUtil;
import sxx.xwl.community.community.util.MailClient;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author sxx_27
 * @create 2022-04-20 20:39
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Value("${community.path.domain}")
    private String domain;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    //处理注册
    public Map<String, Object> register(User user) throws Exception {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (user == null) {
            throw new IllegalAccessException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        //验证是否存在
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在！");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        //注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/&dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //"http://localhost:8888/activation/101/code"
        String url = domain + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    //处理激活账号
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    //处理登录
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        //空值处理
        if (StringUtils.isBlank(username)){
            map.put("usernameMsg", "账号不能为空！");
            return map;
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if (user==null)
        {
            map.put("usernameMsg", "账号不存在！");
            return map;
        }
        if (user.getStatus()==0)
        {
            map.put("usernameMsg", "账号未激活！");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password))
        {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        loginTicket.setStatus(0);
        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    //处理注销
    public void logout(String ticket){
        loginTicketMapper.updateStatus(ticket, 1);
    }

    //查询凭证
    public LoginTicket findLoginTicket(String ticket){
        return loginTicketMapper.selectByticket(ticket);
    }
}
