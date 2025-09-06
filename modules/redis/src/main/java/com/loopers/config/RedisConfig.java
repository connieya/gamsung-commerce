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
import org.springframework.data.redis.serializer.RedisSerializer;
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

    @Primary
    @Bean
    public RedisTemplate<String, String> defaultRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        return defaultRedisTemplate(redisTemplate, lettuceConnectionFactory);
    }

    @Primary
    @Bean
    public RedisTemplate<String, Object> defaultObjectRedisTemplate(LettuceConnectionFactory connectionFactory) {
        return buildObjectRedisTemplate(connectionFactory);
    }

    @Qualifier(REDIS_TEMPLATE_MASTER)
    @Bean
    public RedisTemplate<String, String> masterRedisTemplate(
            @Qualifier(CONNECTION_MASTER) LettuceConnectionFactory lettuceConnectionFactory
    ) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        return defaultRedisTemplate(redisTemplate, lettuceConnectionFactory);
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
        StringRedisSerializer s = StringRedisSerializer.UTF_8;
        template.setKeySerializer(s);
        template.setValueSerializer(s);
        template.setHashKeySerializer(s);
        template.setHashValueSerializer(s);
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    private RedisTemplate<String, Object> buildObjectRedisTemplate(LettuceConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.json());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.json());
        template.setConnectionFactory(connectionFactory);

        return template;
    }


}
