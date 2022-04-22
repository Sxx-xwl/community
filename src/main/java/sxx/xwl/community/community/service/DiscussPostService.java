package sxx.xwl.community.community.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sxx.xwl.community.community.dao.DiscussPostMapper;
import sxx.xwl.community.community.entity.DiscussPost;

import java.util.List;

/**
 * @author sxx_27
 * @create 2022-04-20 20:34
 */
@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussPostsRows(int userId) {
        return discussPostMapper.selectDiscussPostsRows(userId);
    }
}
