package sxx.xwl.community.community.event;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.tencentcloudapi.tci.v20190318.models.Face;
import com.tencentcloudapi.teo.v20220106.models.Zone;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import sxx.xwl.community.community.entity.DiscussPost;
import sxx.xwl.community.community.entity.Event;
import sxx.xwl.community.community.entity.Message;
import sxx.xwl.community.community.service.DiscussPostService;
import sxx.xwl.community.community.service.ESService;
import sxx.xwl.community.community.service.MessageService;
import sxx.xwl.community.community.util.CommunityConstant;
import sxx.xwl.community.community.util.CommunityUtil;
import sxx.xwl.community.community.util.HostHolder;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

/**
 * @author sxx_27
 * @create 2022-05-13 17:08
 */
@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ESService esService;

    //图片存放位置
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${wk.image.command}")
    private String wkImageCommand;


    @Value("${qiniu.key.accessKey}")
    private String accessKey;

    @Value("${qiniu.key.secretKey}")
    private String secretKey;

    @Value("${qiniu.bucket.share.bucketName}")
    private String shareBucketName;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    //点赞、评论、关注事件
    @KafkaListener(topics = {TOPIC_LIKE, TOPIC_FOLLOW, TOPIC_COMMENT})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            LOGGER.error("消息格式错误！");
            return;
        }

        //发送系统通知
        Message message = new Message();
        message.setFromId(SYSTEM_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    //发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            LOGGER.error("消息格式错误！");
            return;
        }
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        esService.saveDiscussPost(post);
    }

    //删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            LOGGER.error("消息格式错误！");
            return;
        }
        esService.deleteDiscussPost(event.getEntityId());
    }

    //分享事件
    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            LOGGER.error("消息内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            LOGGER.error("消息格式错误！");
            return;
        }
//       d:/work/wkhtmltopdf/bin/wkhtmltoimage --quality 75 www.baidu.com d:/work/wk_img/3.png

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;

        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println("ok!");
            LOGGER.info("生成图片成功:" + cmd);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("生成图片失败:" + e.getMessage());
        }

        // 启用定时器，监视该图片，一旦生成，则上传至七牛云
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
    }

    class UploadTask implements Runnable {

        //文件名称
        private String fileName;
        //文件后缀
        private String suffix;
        //启动任务的返回值
        private Future future;
        //开始时间
        private long startTime;
        //上传次数
        private int uploadTime;

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            //生成图片失败
            if (System.currentTimeMillis() - startTime > 30000) {
                LOGGER.error("执行时间过长，终止任务！" + fileName);
                future.cancel(true);
            }
            //上传失败
            if (uploadTime >= 3) {
                LOGGER.error("上传次数过多，终止任务！" + fileName);
                future.cancel(true);
            }
            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists()) {
                LOGGER.info(String.format("开始第%d次上传[%s]", ++uploadTime, fileName));
                //设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                //生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                //指定上传的机房
                UploadManager manager = new UploadManager(new Configuration(Region.huanan()));
                try {
                    //开始上传图片
                    Response response = manager.put(path, fileName, uploadToken,
                            null, "image/" + suffix.substring(suffix.lastIndexOf('.') + 1), false);
                    //处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !"0".equals(json.get("code").toString())) {
                        LOGGER.info(String.format("第%d次上传[%s]失败！", uploadTime, fileName));
                    } else {
                        LOGGER.info(String.format("第%d次上传[%s]成功！", uploadTime, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    LOGGER.info(String.format("第%d次上传[%s]失败！", uploadTime, fileName));
                }
            } else {
                LOGGER.info("等待图片生成！[" + fileName + "]");
            }
        }
    }

}
