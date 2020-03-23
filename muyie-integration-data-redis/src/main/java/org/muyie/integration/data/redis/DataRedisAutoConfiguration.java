package org.muyie.integration.data.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.integration.redis.util.RedisLockRegistry;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;

@Configuration
public class DataRedisAutoConfiguration {

  @Bean
  public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
    return new RedisLockRegistry(redisConnectionFactory, "muyie-r-lock");
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    FastJsonRedisSerializer<?> serializer = new FastJsonRedisSerializer<>(Object.class);
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(serializer);
    redisTemplate.setHashKeySerializer(serializer);
    redisTemplate.setHashValueSerializer(serializer);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

}
