package sxx.xwl.community.community.dao;

import org.apache.ibatis.annotations.Mapper;
import sxx.xwl.community.community.entity.Comment;

import java.util.List;

/**
 * @author sxx_27
 * @create 2022-04-29 15:33
 */
@Mapper
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    int selectCountByEntity(int entityType, int entityId);

}
