package sxx.xwl.community.community;

import org.junit.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/**单元测试
 * @author sxx_27
 * @create 2022-05-27 16:19
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

    @BeforeClass
    public static void beforeClass(){
        System.out.println("beforeClass");
    }

    @AfterClass
    public static void afterClass(){
        System.out.println("afterClass");
    }

    @Before
    public void before(){
        System.out.println("before");
    }

    @After
    public void after(){
        System.out.println("after");
    }

    @Test
    public void test(){
        System.out.println(1);
    }

    @Test
    public void test1(){
        System.out.println(2);
    }
}
