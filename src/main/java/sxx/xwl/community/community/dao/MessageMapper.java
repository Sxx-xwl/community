package sxx.xwl.community.community.dao;

import org.apache.ibatis.annotations.Mapper;
import sxx.xwl.community.community.entity.Message;

import java.util.List;

/**
 * @author sxx_27
 * @create 2022-05-05 10:07
 */
@Mapper
public interface MessageMapper {

    //查询当前用户的会话列表，每个回话返回最新的私信
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询当前用户的会话数量
    int selectConversationCount(int userId);

    //查询某个会话所包含的私信列表
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询某个会话所包含的私信数量
    int selectLetterCount(String conversationId);

    //查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    //发送私信
    int insertMessage(Message message);

    //更改消息状态
    int updateStatus(List<Integer> ids, int status);

}
