package sxx.xwl.community.community.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sxx.xwl.community.community.entity.Event;
import sxx.xwl.community.community.event.EventProducer;
import sxx.xwl.community.community.util.CommunityConstant;
import sxx.xwl.community.community.util.CommunityUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sxx_27
 * @create 2022-05-24 15:57
 */
@Controller
public class ShareController implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShareController.class);

    @Autowired
    private EventProducer eventProducer;

    //域名
    @Value("${community.path.domain}")
    private String domain;

    //域名
    @Value("${server.servlet.context-path}")
    private String contextPath;

    //图片存放位置
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @RequestMapping(value = "/share", method = RequestMethod.GET)
    @ResponseBody
    public String share(String htmlUrl) {
        //文件名
        String fileName = CommunityUtil.generateUUID();

        //异步生成长图
        Event event = new Event()
                .setTopic(TOPIC_SHARE)
                .setData("htmlUrl", htmlUrl)
                .setData("fileName", fileName)
                .setData("suffix", ".png");
        eventProducer.fireEvent(event);

        Map<String, Object> map = new HashMap<>();
        map.put("shareUrl", domain + "/share/image/" + fileName);

        return CommunityUtil.getJSONString(0, null, map);
    }

    //获取长图
    @RequestMapping(value = "/share/image/{fileName}", method = RequestMethod.GET)
    public void getShareImage(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("文件名不能为空！");
        }

        response.setContentType("image/png");
        File file = new File(wkImageStorage + "/" + fileName + ".png");
        try {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            LOGGER.error("获取长图失败：" + e.getMessage());
        }
    }
}
