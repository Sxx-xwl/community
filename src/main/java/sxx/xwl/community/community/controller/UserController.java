package sxx.xwl.community.community.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import sxx.xwl.community.community.annotation.LoginRequired;
import sxx.xwl.community.community.entity.User;
import sxx.xwl.community.community.service.FollowService;
import sxx.xwl.community.community.service.LikeService;
import sxx.xwl.community.community.service.UserService;
import sxx.xwl.community.community.util.CommunityConstant;
import sxx.xwl.community.community.util.CommunityUtil;
import sxx.xwl.community.community.util.HostHolder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author sxx_27
 * @create 2022-04-25 13:20
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImg, Model model) {
        //判断是否选择了图片
        if (headerImg == null) {
            model.addAttribute("error", "您还没选择图片！");
            return "/site/setting";
        }
        String filename = headerImg.getOriginalFilename();
        String substring = filename.substring(filename.lastIndexOf(".") + 1);
        //判断图片后缀是否正确
        if (StringUtils.isBlank(substring)) {
            model.addAttribute("error", "文件格式不正确！");
            return "/site/setting";
        }
        //生成随机文件名
        filename = CommunityUtil.generateUUID() + substring;
        //确定文件存放路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            //存储文件
            headerImg.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器异常!" + e);
        }
        //更新当前用户头像的路径
        //http://localhost:8888/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + "/user/header/" + filename;
        int i = userService.updateHeader(user.getId(), headerUrl);
        System.out.println("更新头像成功！" + i);
        return "redirect:/index";
    }

    @RequestMapping(value = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        //找到图片存放路径
        filename = uploadPath + "/" + filename;
        //声明输出文件的格式
        String substring = filename.substring(filename.lastIndexOf(".") + 1);
        response.setContentType("image/" + substring);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filename);
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            LOGGER.error("读取头像失败！" + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(value = "/updatePassword", method = RequestMethod.POST)
    public String updatePassword(Model model, String password, String newPassword1, String newPassword2) {
        User user = hostHolder.getUser();
        String oldPassword = user.getPassword();
        String salt = user.getSalt();
        String oldpassword = new String(password);
        password = CommunityUtil.md5(password + salt);
        //对密码的处理
        if (StringUtils.isBlank(password) || !password.equals(oldPassword)) {
            model.addAttribute("passwordMsg", "原密码不正确！");
            return "/site/setting";
        }
        //对新密码的处理
        if (StringUtils.isBlank(newPassword1)) {
            model.addAttribute("newPassword1Msg", "新密码格式错误！");
            return "/site/setting";
        }
        int length = newPassword1.length();
        if (newPassword1.replaceAll(" ", "").length() != length) {
            model.addAttribute("newPassword1Msg", "新密码不能包含空格！");
            return "/site/setting";
        } else if (length < 8) {
            model.addAttribute("newPassword1Msg", "新密码长度不可小于8！");
            return "/site/setting";
        } else if (newPassword1.equals(oldpassword)) {
            model.addAttribute("newPassword1Msg", "新密码不可与原密码相同！");
            return "/site/setting";
        }
        if (StringUtils.isBlank(newPassword2) || !newPassword1.equals(newPassword2)) {
            model.addAttribute("newPassword2Msg", "两次密码不相同！");
            return "/site/setting";
        }
        newPassword1 = CommunityUtil.md5(newPassword1 + salt);
        userService.updatePassword(user, newPassword1);
        return "redirect:/index";
    }

    //个人主页
    @RequestMapping(value = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfile(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在！");
        }

        model.addAttribute("user", user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        //粉丝数
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        //是否关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }


}
