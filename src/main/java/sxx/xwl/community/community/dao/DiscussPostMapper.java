package sxx.xwl.community.community.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import sxx.xwl.community.community.entity.DiscussPost;

import java.util.List;

/**
 * @author sxx_27
 * @create 2022-04-20 20:07
 */
@Mapper
public interface DiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit , int orderMode);

    int selectDiscussPostsRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    int updateType(int id,int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);

}
