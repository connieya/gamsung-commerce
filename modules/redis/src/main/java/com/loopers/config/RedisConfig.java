package com.loopers.config;

import io.lettuce.core.ReadFrom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.function.Consumer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    public static final String REDIS_TEMPLATE_MASTER = "redisTemplateMaster";
    private static final String CONNECTION_MASTER = "redisConnectionMaster";

    private final RedisProperties redisProperties;

    @Primary
    @Bean
    public LettuceConnectionFactory defaultRedisConnectionFactory() {
        return lettuceConnectionFactory(
                redisProperties,
                b -> b.readFrom(ReadFrom.REPLICA_PREFERRED)
        );
    }

    @Qualifier(CONNECTION_MASTER)
    @Bean
    public LettuceConnectionFactory masterRedisConnectionFactory() {
        return lettuceConnectionFactory(
                redisProperties,
                b -> b.readFrom(ReadFrom.MASTER)
        );
    }


    @Bean
    public RedisTemplate<String, String> defaultRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        return defaultRedisTemplate(redisTemplate, lettuceConnectionFactory);
    }


    @Qualifier(REDIS_TEMPLATE_MASTER)
    @Bean
    public RedisTemplate<String, String> masterRedisTemplate(
            @Qualifier(CONNECTION_MASTER) LettuceConnectionFactory connectionFactory
    ) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        return defaultRedisTemplate(redisTemplate, connectionFactory);
    }

    @Primary
    @Bean
    public RedisTemplate<String, Object> genericRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());

        // 모든 객체를 JSON으로 직렬화하는 Serializer 사용
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return redisTemplate;
    }

    private LettuceConnectionFactory lettuceConnectionFactory(
            RedisProperties properties,
            Consumer<LettuceClientConfiguration.LettuceClientConfigurationBuilder> customizer
    ) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();
        if (customizer != null) {
            customizer.accept(builder);
        }

        LettuceClientConfiguration clientConfig = builder.build();
        RedisStaticMasterReplicaConfiguration masterReplicaConfig = new RedisStaticMasterReplicaConfiguration(
                properties.master().host(), properties.master().port()
        );
        masterReplicaConfig.setDatabase(properties.databse());
        for (RedisNodeInfo r : properties.replicas()) {
            masterReplicaConfig.addNode(r.host(), r.port());
        }
        return new LettuceConnectionFactory(masterReplicaConfig, clientConfig);
    }


    private <K, V> RedisTemplate<K, V> defaultRedisTemplate(
            RedisTemplate<K, V> template,
            LettuceConnectionFactory connectionFactory
    ) {
        StringRedisSerializer stringRedisSerializer = StringRedisSerializer.UTF_8;
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}

