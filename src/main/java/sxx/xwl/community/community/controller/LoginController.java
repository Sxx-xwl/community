package sxx.xwl.community.community.controller;

import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import sxx.xwl.community.community.entity.User;
import sxx.xwl.community.community.service.UserService;
import sxx.xwl.community.community.util.*;


import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author sxx_27
 * @create 2022-04-22 20:16
 */
@Controller
public class LoginController implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private UserService userService;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private RedisTemplate redisTemplate;


    /**
     * 跳转到注册页
     *
     * @return {@link String}
     * @author sxx
     * <br>CreateDate 2022-04-22 21:10
     */
    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    /**
     * 进行注册
     *
     * @param model
     * @return {@link String}
     * @author sxx
     * <br>CreateDate 2022-04-22 21:10
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) throws Exception {
        Map<String, Object> map = userService.register(user);
        if (map == null || map.isEmpty()) {
            model.addAttribute("msg", "注册成功，我们已经向您的邮箱发送了一封激活邮件，请您尽快激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    //"http://localhost:8888/activation/101/code"
    @RequestMapping(value = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功，您可以正常使用了！");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作，该账号已经激活！");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败，激活码错误！");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }

    /**
     * 获取验证码
     *
     * @param response
     * @author sxx
     * <br>CreateDate 2022-05-12 13:27
     */
    @RequestMapping(value = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        //生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        //将验证码text存入session
        //session.setAttribute("kaptcha", text);

        //验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        //设置有效时长
        cookie.setMaxAge(60);
        //设置有效路径
        cookie.setPath(contextPath);
        response.addCookie(cookie);

        //将验证码存入redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        //将得到的图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            LOGGER.error("响应验证码失败：" + e.getMessage());
        }
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(Model model, String username, String password, String code,
                        boolean remember/*, HttpSession session*/, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {
        //检查验证码
//        String kaptcha = (String) session.getAttribute("kaptcha");

        String kaptcha = null;
        if (!StringUtils.isBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }
        //检查账号密码
        int expiredSeconds = remember ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @RequestMapping(value = "/forget", method = RequestMethod.GET)
    public String forget() {
        return "/site/forget";
    }

    @RequestMapping(value = "/getCode", method = RequestMethod.GET)
    @ResponseBody
    public String getCode(String email, Model model, HttpSession session) {
        //发送重置邮件
        if (StringUtils.isBlank(email)) {
            model.addAttribute("emailMsg", "邮箱不能为空！");
            return "/site/forget";
        }
        User user = userService.selectByEmail(email);
        if (user == null) {
            model.addAttribute("emailMsg", "该邮箱未被注册！");
            return "/site/forget";
        }

        // 发送邮件
        Context context = new Context();
        context.setVariable("email", email);
        String code = CommunityUtil.generateUUID().substring(0, 5);
        context.setVariable("code", code);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "找回密码", content);

        // 保存验证码
        session.setAttribute("verifyCode", code);
        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    public String reset(String email, HttpServletRequest request, String code, Model model, String password) {

        String context = request.getParameter("context");
        if (StringUtils.isBlank(code)) {
            model.addAttribute("codeMsg", "请填写验证码！");
            return "/site/forget";
        }
        if (!code.equals(context)) {
            model.addAttribute("codeMsg", "验证码输入错误！");
            return "/site/forget";
        }
        User user = userService.selectByEmail(email);
        Map<String, Object> map = userService.updatePassword(user, password);
        if (map != null) {
            model.addAttribute("passwordMsg", map.get("newPassword1Msg"));
            return "/site/forget";
        } else {
            return "/site/login";
        }
    }
}