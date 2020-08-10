package org.muyie.integration.data.redis;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.integration.redis.util.RedisLockRegistry;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;

@Configuration
@EnableCaching
public class DataRedisAutoConfigure {

  private final RedisConnectionFactory redisConnectionFactory;

  public DataRedisAutoConfigure(RedisConnectionFactory redisConnectionFactory) {
    this.redisConnectionFactory = redisConnectionFactory;
  }

  /**
   * 基于 Spring Integration Redis 实现的分布式锁
   * 
   * @param redisConnectionFactory 连接工厂对象
   * @return RedisLockRegistry 锁对象
   */
  @Bean
  @ConditionalOnMissingBean
  public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
    return new RedisLockRegistry(redisConnectionFactory, "muyie-rlock");
  }

  @Bean
  @ConditionalOnMissingBean(name = "redisTemplate")
  public RedisTemplate<Object, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(redisConnectionFactory);

    // 自定义序列化配置（FastJSON）
    StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
    FastJsonRedisSerializer<?> fastJsonRedisSerializer =
        new FastJsonRedisSerializer<>(Object.class);
    redisTemplate.setConnectionFactory(redisConnectionFactory);
    redisTemplate.setKeySerializer(stringRedisSerializer);
    redisTemplate.setHashKeySerializer(stringRedisSerializer);
    redisTemplate.setValueSerializer(fastJsonRedisSerializer);
    redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

  /**
   * 自定义新的键名生成规则，默认的生成规则：{@link SimpleKeyGenerator}
   * 
   * @return 新的键名生成规则
   */
  @Bean
  @ConditionalOnMissingBean
  public KeyGenerator keyGenerator() {
    return (target, method, params) -> {
      StringBuilder sb = new StringBuilder();
      sb.append(target.getClass().getName());
      sb.append("::" + method.getName() + ":");
      Arrays.asList(params).forEach(obj -> sb.append(obj.toString()));
      return sb.toString();
    };
  }

  @Bean
  @ConditionalOnMissingBean
  public CacheManager cacheManager() {
    RedisCacheManager cacheManager =
        RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory).build();
    return cacheManager;
  }

}
