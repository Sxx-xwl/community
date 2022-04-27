package sxx.xwl.community.community.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sxx.xwl.community.community.entity.DiscussPost;
import sxx.xwl.community.community.entity.User;
import sxx.xwl.community.community.service.DiscussPostService;
import sxx.xwl.community.community.service.UserService;
import sxx.xwl.community.community.util.CommunityUtil;
import sxx.xwl.community.community.util.HostHolder;

import java.util.Date;

/**
 * @author sxx_27
 * @create 2022-04-26 19:29
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @ResponseBody
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "您还没有登录！");
        }
        DiscussPost post = new DiscussPost();
        post.setTitle(title);
        post.setContent(content);
        post.setUserId(user.getId());
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);
        //报错统一处理！
        return CommunityUtil.getJSONString(0, "success!");
    }

    @RequestMapping(value = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model) {
        //帖子
        DiscussPost Post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", Post);
        //作者
        User user = userService.findUserById(Post.getUserId());
        model.addAttribute("user", user);
        return "/site/discuss-detail";
    }
}
