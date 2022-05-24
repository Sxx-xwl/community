package sxx.xwl.community.community.event;

import com.alibaba.fastjson.JSONObject;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import sxx.xwl.community.community.entity.DiscussPost;
import sxx.xwl.community.community.entity.Event;
import sxx.xwl.community.community.entity.Message;
import sxx.xwl.community.community.service.DiscussPostService;
import sxx.xwl.community.community.service.ESService;
import sxx.xwl.community.community.service.MessageService;
import sxx.xwl.community.community.util.CommunityConstant;
import sxx.xwl.community.community.util.HostHolder;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
    }

}
