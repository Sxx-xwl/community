package sxx.xwl.community.community.controller;

import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
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
import org.springframework.web.bind.annotation.ResponseBody;
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

    @Value("${qiniu.key.accessKey}")
    private String accessKey;

    @Value("${qiniu.key.secretKey}")
    private String secretKey;

    @Value("${qiniu.bucket.header.bucketName}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        //??????????????????
        String fileName = CommunityUtil.generateUUID();
        //??????????????????
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        //??????????????????
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);
        return "/site/setting";
    }

    //??????????????????
    @RequestMapping(value = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "????????????????????????");
        }
        String url = headerBucketUrl + "/" + fileName;
        userService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }

    //??????
    @LoginRequired
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImg, Model model) {
        //???????????????????????????
        if (headerImg == null) {
            model.addAttribute("error", "????????????????????????");
            return "/site/setting";
        }
        String filename = headerImg.getOriginalFilename();
        String substring = filename.substring(filename.lastIndexOf(".") + 1);
        //??????????????????????????????
        if (StringUtils.isBlank(substring)) {
            model.addAttribute("error", "????????????????????????");
            return "/site/setting";
        }
        //?????????????????????
        filename = CommunityUtil.generateUUID() + substring;
        //????????????????????????
        File dest = new File(uploadPath + "/" + filename);
        try {
            //????????????
            headerImg.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error("??????????????????" + e.getMessage());
            throw new RuntimeException("????????????????????????????????????!" + e);
        }
        //?????????????????????????????????
        //http://localhost:8888/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + "/user/header/" + filename;
        int i = userService.updateHeader(user.getId(), headerUrl);
        System.out.println("?????????????????????" + i);
        return "redirect:/index";
    }

    //??????
    @RequestMapping(value = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        //????????????????????????
        filename = uploadPath + "/" + filename;
        //???????????????????????????
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
            LOGGER.error("?????????????????????" + e.getMessage());
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
        //??????????????????
        if (StringUtils.isBlank(password) || !password.equals(oldPassword)) {
            model.addAttribute("passwordMsg", "?????????????????????");
            return "/site/setting";
        }
        //?????????????????????
        if (StringUtils.isBlank(newPassword1)) {
            model.addAttribute("newPassword1Msg", "????????????????????????");
            return "/site/setting";
        }
        int length = newPassword1.length();
        if (newPassword1.replaceAll(" ", "").length() != length) {
            model.addAttribute("newPassword1Msg", "??????????????????????????????");
            return "/site/setting";
        } else if (length < 8) {
            model.addAttribute("newPassword1Msg", "???????????????????????????8???");
            return "/site/setting";
        } else if (newPassword1.equals(oldpassword)) {
            model.addAttribute("newPassword1Msg", "????????????????????????????????????");
            return "/site/setting";
        }
        if (StringUtils.isBlank(newPassword2) || !newPassword1.equals(newPassword2)) {
            model.addAttribute("newPassword2Msg", "????????????????????????");
            return "/site/setting";
        }
        newPassword1 = CommunityUtil.md5(newPassword1 + salt);
        userService.updatePassword(user, newPassword1);
        return "redirect:/index";
    }

    //????????????
    @RequestMapping(value = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfile(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("?????????????????????");
        }

        model.addAttribute("user", user);
        //????????????
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //????????????
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        //?????????
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        //????????????
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "/site/profile";
    }


}
