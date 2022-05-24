package sxx.xwl.community.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * @author sxx_27
 * @create 2022-05-24 15:46
 */
@Configuration
public class WkConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @PostConstruct
    public void init(){
        //创建wk图片目录
        File file = new File(wkImageStorage);
        if (!file.exists()){
            //创建目录文件夹
            file.mkdir();
            LOGGER.info("创建wk图片目录:" + wkImageStorage);
        }else {
            LOGGER.info("wk图片目录存在:" + wkImageStorage);
        }
    }
}