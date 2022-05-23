package sxx.xwl.community.community.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import sxx.xwl.community.community.entity.DiscussPost;
import sxx.xwl.community.community.service.DiscussPostService;
import sxx.xwl.community.community.service.ESService;
import sxx.xwl.community.community.service.LikeService;
import sxx.xwl.community.community.util.CommunityConstant;
import sxx.xwl.community.community.util.RedisKeyUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author sxx_27
 * @create 2022-05-23 20:22
 */
public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostScoreRefreshJob.class);


    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ESService esService;

    //牛客纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败！", e);
        }
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String postScoreKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(postScoreKey);
        if (operations.size() == 0) {
            LOGGER.info("任务取消！无刷新帖子！");
            return;
        }
        LOGGER.info("任务开始：正在刷新帖子分数：" + operations.size());

        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }

        LOGGER.info("任务结束：帖子分数刷新完毕！");
    }

    private void refresh(int postId) {

        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            LOGGER.error("该帖子不存在！id=" + postId);
            return;
        } else if (post.getStatus() == 2) {
            LOGGER.error("该帖子已被删除！id=" + postId);
            return;
        }

        //是否加精
        boolean wonderful = post.getStatus() == 1;
        //评论数
        int commentCount = post.getCommentCount();
        //点赞数
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        //计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //分数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 60 * 60 * 24);

        //更新帖子分数
        discussPostService.updateScore(postId, score);
        post.setScore(score);
        //同步搜索数据
        esService.saveDiscussPost(post);
    }

}
