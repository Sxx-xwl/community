package sxx.xwl.community.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import sxx.xwl.community.community.dao.AlphaDao;
import sxx.xwl.community.community.service.AlphaService;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
class CommunityApplicationTests implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Test
    void contextLoads() {
        System.out.println(applicationContext);

        AlphaDao alphaDao = applicationContext.getBean(AlphaDao.class);
        System.out.println(alphaDao.selsect());

        alphaDao = applicationContext.getBean("hibernate", AlphaDao.class);
        System.out.println(alphaDao.selsect());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Test
    public void test1() {

        AlphaService alphaService = applicationContext.getBean(AlphaService.class);
        System.out.println(alphaService);
    }

    @Test
    public void test2() {

        SimpleDateFormat beans = applicationContext.getBean(SimpleDateFormat.class);
        System.out.println(beans.format(new Date()));
    }

    @Autowired
    private AlphaDao alphaDao;
    @Autowired
    private SimpleDateFormat simpleDateFormat;

    @Test
    public void test3() {
        System.out.println(alphaDao);
        System.out.println(simpleDateFormat.format(new Date()));
    }
}
