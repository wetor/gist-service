package top.wetor.gist;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import top.wetor.gist.model.GistMetadata;
import top.wetor.gist.model.User;

@SpringBootTest
class GistApplicationTests {

    @Autowired
    public RedisTemplate redisTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    public void RedisTest(){
        User user = new User();
        GistMetadata gistMetadata = new GistMetadata();
        redisTemplate.opsForValue().set("test",gistMetadata);
    }

}
