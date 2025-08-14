package config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "datasource.redis")
public record RedisProperties(
        int databse ,
        RedisNodeInfo master,
        List<RedisNodeInfo> replicas
) {
}
