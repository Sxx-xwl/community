package sxx.xwl.community.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import sxx.xwl.community.community.service.AlphaService;

/**
 * @author sxx_27
 * @create 2022-04-27 15:15
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class TransactionalTest {

    @Autowired
    private AlphaService alphaService;

    @Test
    public void test1(){
        System.out.println(alphaService.save1());
    }

    @Test
    public void test2(){
        System.out.println(alphaService.save2());
    }

}
