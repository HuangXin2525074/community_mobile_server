package com.mycomany.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        //config key type.
        template.setKeySerializer(RedisSerializer.string());

        //config value type.
        template.setValueSerializer(RedisSerializer.json());

        //config hash key type
        template.setHashKeySerializer(RedisSerializer.string());

        //config hash value type
        template.setHashValueSerializer(RedisSerializer.json());

        // confirm config
        template.afterPropertiesSet();

        return template;

    }
}
