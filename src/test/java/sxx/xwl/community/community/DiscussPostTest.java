package sxx.xwl.community.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import sxx.xwl.community.community.CommunityApplication;
import sxx.xwl.community.community.dao.DiscussPostMapper;
import sxx.xwl.community.community.entity.DiscussPost;

import java.util.Arrays;
import java.util.List;

/**
 * @author sxx_27
 * @create 2022-04-20 20:23
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class DiscussPostTest {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void test(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10,0);
        discussPosts.forEach(System.out::println);

        int i = discussPostMapper.selectDiscussPostsRows(0);
        System.out.println(i);
    }

}
