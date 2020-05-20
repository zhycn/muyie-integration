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
   * 解决apollo无法动态刷新ConfigurationProperties注解的类
   */
  @ApolloConfigChangeListener
  public void onChange(ConfigChangeEvent event) {
    refresh(event);
  }

  private void refresh(ConfigChangeEvent event) {
    event.changedKeys().forEach(key -> {
      ConfigChange change = event.getChange(key);
      log.info("Apollo change - {}", change.toString());
    });

    // 更新相应的bean的属性值，主要是存在@ConfigurationProperties注解的bean
    SpringContextHolder.getApplicationContext()
        .publishEvent(new EnvironmentChangeEvent(event.changedKeys()));
  }

}
