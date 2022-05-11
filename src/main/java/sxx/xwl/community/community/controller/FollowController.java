package sxx.xwl.community.community.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sxx.xwl.community.community.entity.User;
import sxx.xwl.community.community.service.FollowService;
import sxx.xwl.community.community.util.CommunityUtil;
import sxx.xwl.community.community.util.HostHolder;

/**
 * @author sxx_27
 * @create 2022-05-11 11:05
 */
@Controller
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(value = "/follow",method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0,"已关注!");
    }

    @RequestMapping(value = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0,"已取消关注!");
    }

}
