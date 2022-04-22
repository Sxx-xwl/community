package sxx.xwl.community.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import sxx.xwl.community.community.util.MailClient;

/**
 * @author sxx_27
 * @create 2022-04-22 16:45
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Test
    public void test(){
        mailClient.sendMail("1905921852@qq.com","test","hello");
    }

}
