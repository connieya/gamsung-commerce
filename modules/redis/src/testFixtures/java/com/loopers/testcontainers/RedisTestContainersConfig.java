package com.loopers.testcontainers;

import com.redis.testcontainers.RedisContainer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisTestContainersConfig {

    private static final RedisContainer redisContainer;

    static {
        redisContainer = new RedisContainer(RedisContainer.DEFAULT_IMAGE_NAME.withTag("8"));
        redisContainer.start();
    }

    public RedisTestContainersConfig() {
        System.setProperty("datasource.redis.database", "0");
        System.setProperty("datasource.redis.master.host", redisContainer.getHost());
        System.setProperty("datasource.redis.host.port", String.valueOf(redisContainer.getFirstMappedPort()));
        System.setProperty("datasource.redis.replicas[0].host", redisContainer.getHost());
        System.setProperty("datasource.redis.replicas[0].port", String.valueOf(redisContainer.getFirstMappedPort()));
    }

}
