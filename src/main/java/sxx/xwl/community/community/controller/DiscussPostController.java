package sxx.xwl.community.community.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sxx.xwl.community.community.entity.*;
import sxx.xwl.community.community.event.EventProducer;
import sxx.xwl.community.community.service.CommentService;
import sxx.xwl.community.community.service.DiscussPostService;
import sxx.xwl.community.community.service.LikeService;
import sxx.xwl.community.community.service.UserService;
import sxx.xwl.community.community.util.CommunityConstant;
import sxx.xwl.community.community.util.CommunityUtil;
import sxx.xwl.community.community.util.HostHolder;

import java.util.*;

/**
 * @author sxx_27
 * @create 2022-04-26 19:29
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

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
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        //报错统一处理！
        return CommunityUtil.getJSONString(0, "success!");
    }

    @RequestMapping(value = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        //帖子
        DiscussPost Post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", Post);
        //作者
        User user = userService.findUserById(Post.getUserId());
        model.addAttribute("user", user);
        //点赞
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, Post.getId());
        model.addAttribute("likeCount", likeCount);
        int likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);
        //评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(Post.getCommentCount());

        //分页查询
        //评论列表
        List<Comment> comments = commentService.findCommentsByEntity(ENTITY_TYPE_POST, Post.getId(), page.getOffset(), page.getLimit());
        //评论显示对象的列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                //单个评论的内容
                Map<String, Object> map = new HashMap<>();
                //评论
                map.put("comment", comment);
                //评论的主人
                map.put("user", userService.findUserById(comment.getUserId()));
                //点赞
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                map.put("likeCount", likeCount);
                likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(),
                        ENTITY_TYPE_COMMENT, comment.getId());
                map.put("likeStatus", likeStatus);
                //回复列表
                List<Comment> subComments = commentService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(),
                        0, Integer.MAX_VALUE);
                //回复显示对象的列表
                List<Map<String, Object>> subCommentVoList = new ArrayList<>();
                if (subComments != null) {
                    for (Comment subComment : subComments) {
                        //单个回复内容
                        Map<String, Object> subMap = new HashMap<>();
                        //回复
                        subMap.put("subComment", subComment);
                        //回复的作者
                        subMap.put("user", userService.findUserById(subComment.getUserId()));
                        //回复的目标
                        User target = subComment.getTargetId() == 0 ? null : userService.findUserById(subComment.getTargetId());
                        subMap.put("target", target);
                        //点赞
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, subComment.getId());
                        subMap.put("likeCount", likeCount);
                        likeStatus = hostHolder.getUser() == null ? 0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(),
                                ENTITY_TYPE_COMMENT, subComment.getId());
                        subMap.put("likeStatus", likeStatus);
                        //添加到回复显示对象列表
                        subCommentVoList.add(subMap);
                    }
                }
                map.put("replys", subCommentVoList);
                //回复数
                int commentCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                map.put("commentCount", commentCount);
                //添加到显示对象列表
                commentVoList.add(map);
            }
        }
        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }

    //置顶
    @RequestMapping(value = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);
        //同步到es
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    //加精
    @RequestMapping(value = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);
        //同步到es
        //触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    //删除
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);
        //同步到es
        //触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
