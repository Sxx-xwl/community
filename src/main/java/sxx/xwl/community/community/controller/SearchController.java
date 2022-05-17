package sxx.xwl.community.community.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sxx.xwl.community.community.entity.DiscussPost;
import sxx.xwl.community.community.entity.Page;
import sxx.xwl.community.community.service.ESService;
import sxx.xwl.community.community.service.LikeService;
import sxx.xwl.community.community.service.UserService;
import sxx.xwl.community.community.util.CommunityConstant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sxx_27
 * @create 2022-05-17 13:57
 */
@Controller
public class SearchController implements CommunityConstant {

    @Autowired
    private ESService esService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    //search?keyword=xxx
    @RequestMapping(value = "/search" ,method = RequestMethod.GET)
    public String search(String keyword , Page page , Model model) throws IOException {
        //搜索帖子
        List<DiscussPost> searchResult = esService.searchDiscussPost(keyword, page.getCurrent() - 1, 10);
        //聚合数据
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if (searchResult!=null){
            for (DiscussPost post : searchResult){
                Map<String,Object> map = new HashMap<>();
                //帖子
                map.put("post", post);
                //作者
                map.put("user", userService.findUserById(post.getUserId()));
                //点赞数
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId()));

                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        System.out.println();
        //分页
        page.setPath("/search?keyword=" + keyword);
        page.setRows(searchResult == null? 0 : searchResult.size());

        return "/site/search";
    }

}
