package org.muyie.integration.apollo;

import org.muyie.framework.config.spring.SpringContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.annotation.Configuration;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;

@Configuration
@EnableApolloConfig
public class ApolloAutoConfiguration {

  private static final Logger log = LoggerFactory.getLogger(ApolloAutoConfiguration.class);

  /**
   * 解决 Apollo 无法自动刷新 @ConfigurationProperties 注解类的BUG
   */
  @ApolloConfigChangeListener
  public void onChange(ConfigChangeEvent event) {
    refreshScope(event);
  }

  private void refreshScope(ConfigChangeEvent event) {
    event.changedKeys().forEach(key -> {
      ConfigChange configChange = event.getChange(key);
      log.info("Apollo ConfigChangeEvent - {}", configChange);
    });

    // 更新相应的 Bean 的属性值，主要是存在 @ConfigurationProperties 注解的 Bean
    SpringContextHolder.getApplicationContext()
        .publishEvent(new EnvironmentChangeEvent(event.changedKeys()));
  }

}
