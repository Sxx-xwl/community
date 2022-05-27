package sxx.xwl.community.community.actuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;
import sxx.xwl.community.community.util.CommunityUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author sxx_27
 * @create 2022-05-27 17:10
 */
@Component
@Endpoint(id = "database")
public class DatabaseEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseEndpoint.class);

    @Autowired
    private DataSource dataSource;

    @ReadOperation
    public String checkConnection() {
        try (
                Connection conn = dataSource.getConnection();
        ) {
            return CommunityUtil.getJSONString(0,"获取连接成功！");
        } catch (SQLException e) {
            LOGGER.error("获取连接失败:" + e.getMessage());
            return CommunityUtil.getJSONString(0,"获取连接失败！");
        }
    }

}
