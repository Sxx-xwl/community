package sxx.xwl.community.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author sxx_27
 * @create 2022-05-09 17:52
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void test1(){
        String redisKey = "count";

        //添加key：value
        redisTemplate.opsForValue().set(redisKey, 1);
        //获取库中的value
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        //使库中的value+1
        System.out.println(redisTemplate.opsForValue().increment(redisKey));

        String hashRedisKey = "user";

        //添加hash 的 key：value
        redisTemplate.opsForHash().put(hashRedisKey, "id", 2);
        redisTemplate.opsForHash().put(hashRedisKey, "username", "zhangsan");

        System.out.println(redisTemplate.opsForHash().get(hashRedisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(hashRedisKey,"username"));

    }

    //多次访问同一个key
    @Test
    public void test2(){
        String redisKey = "count";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);
        operations.increment();
        System.out.println(operations.get());
    }

}
