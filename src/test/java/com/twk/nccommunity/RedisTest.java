package com.twk.nccommunity;

import com.twk.nccommunity.config.RedisConfig;
import net.bytebuddy.build.ToStringPlugin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@ContextConfiguration(classes = NcCommunityApplication.class)
public class RedisTest {
    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void testValue(){
        String redisKey = "test:stu";
        redisTemplate.opsForValue().set(redisKey,1);

        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHash(){
        String hashKey = "test:member";

        redisTemplate.opsForHash().put(hashKey,"id",1);
        redisTemplate.opsForHash().put(hashKey,"username","helloWorld");

        System.out.println(redisTemplate.opsForHash().get(hashKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(hashKey, "username"));
    }

    @Test
    public void testTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                operations.multi();
                operations.opsForSet().add(redisKey,"hello4");
                operations.opsForSet().add(redisKey,"hello5");
                operations.opsForSet().add(redisKey,"hello6");
                System.out.println(operations.opsForSet().members(redisKey));
                return operations.exec();
            }
        });
        System.out.println(obj);
    }
}
