package sxx.xwl.community.community.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sxx.xwl.community.community.entity.Comment;
import sxx.xwl.community.community.service.CommentService;
import sxx.xwl.community.community.util.HostHolder;

import java.io.PipedReader;
import java.util.Date;

/**
 * @author sxx_27
 * @create 2022-05-04 21:11
 */
@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(value = "/add/{discussPostId}",method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId")int discussPostId, Comment comment){
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.insertComment(comment);

        return "redirect:/discuss/detail/" + discussPostId;
    }

}
