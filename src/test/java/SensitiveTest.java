import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import sxx.xwl.community.community.CommunityApplication;
import sxx.xwl.community.community.util.SensitiveFilter;

/**
 * @author sxx_27
 * @create 2022-04-26 15:08
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void test1(){
        String s = sensitiveFilter.filter("这里不可以赌博吸毒***，吸|毒，嫖娼，更不可以开.开票");
//        String s1 = sensitiveFilter.filter1("这里不可以赌赌博，吸|毒，嫖娼，更不可以开.开票");
        System.out.println(s);
//        System.out.println(s1);
    }

}
