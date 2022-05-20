package sxx.xwl.community.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisCallback;
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

    @Test
    public void test3(){
        String redisKey = "test:hll:01";
        for (int i = 0; i < 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }
        for (int i = 0; i < 100000; i++) {
            int a = (int) (Math.random()*100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, a);
        }
        Long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
    }

    @Test
    public void test4(){
        String redisKey1 = "test:hll:02";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey1, i);
        }
        String redisKey2 = "test:hll:03";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }
        String redisKey3 = "test:hll:04";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }
        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey1,redisKey2,redisKey3);
        Long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }

    @Test
    public void test5(){
        String redisKey = "test:bm:01";

        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));

        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {

                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);
    }
}
