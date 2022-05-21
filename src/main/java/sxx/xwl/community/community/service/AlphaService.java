package sxx.xwl.community.community.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import sxx.xwl.community.community.dao.AlphaDao;
import sxx.xwl.community.community.dao.DiscussPostMapper;
import sxx.xwl.community.community.dao.UserMapper;
import sxx.xwl.community.community.entity.DiscussPost;
import sxx.xwl.community.community.entity.User;
import sxx.xwl.community.community.util.CommunityUtil;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

/**
 * @author sxx_27
 * @create 2022-04-18 14:37
 */
@Service
public class AlphaService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AlphaService.class);

    @Autowired
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Object save1() {
        //先增加用户 再增加帖子
        User user = new User();
        user.setUsername("alpha");
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
        user.setEmail("123456@qq.com");
        user.setHeaderUrl("https://image.nowcoder.com/head/99t.png");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello!");
        post.setContent("新人报到！");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);

        int a = 10 / 0;

        return "ok";
    }

    public Object save2() {
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //先增加用户 再增加帖子
                User user = new User();
                user.setUsername("alpha");
                user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
                user.setPassword(CommunityUtil.md5("123" + user.getSalt()));
                user.setEmail("123456@qq.com");
                user.setHeaderUrl("https://image.nowcoder.com/head/99t.png");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("Hello!");
                post.setContent("新人报到！");
                post.setCreateTime(new Date());
                discussPostMapper.insertDiscussPost(post);

                int a = 10 / 0;

                return "ok";
            }
        });
    }

    public String find() {
        return alphaDao.selsect();
    }

    public AlphaService() {
        System.out.println("实例化AlphaService");
    }

    @PostConstruct
    public void init() {
        System.out.println("初始化AlphaService");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("销毁AlphaService");
    }

    @Async
    public void execute1(){
        LOGGER.debug("execute1");
    }

//    @Scheduled(initialDelay = 10000,fixedRate = 1000)
//    public void execute2(){
//        LOGGER.debug("execute2");
//    }

}
