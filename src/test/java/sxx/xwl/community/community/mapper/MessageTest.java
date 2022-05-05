package sxx.xwl.community.community.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import sxx.xwl.community.community.CommunityApplication;
import sxx.xwl.community.community.dao.MessageMapper;
import sxx.xwl.community.community.entity.Message;

import java.util.List;

/**
 * @author sxx_27
 * @create 2022-05-05 10:54
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MessageTest {

    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void test(){
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message m : messages){
            System.out.println(m);
        }
        System.out.println(messageMapper.selectConversationCount(111));
        List<Message> messages1 = messageMapper.selectLetters("111_112", 0, 20);
        for (Message m : messages1){
            System.out.println(m);
        }
        System.out.println(messageMapper.selectLetterCount("111_112"));
        System.out.println(messageMapper.selectLetterUnreadCount(111, "111_112"));
    }

}
