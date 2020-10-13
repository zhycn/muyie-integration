package org.muyie.integration.data.mybatis;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

@Configuration
public class DataMybatisAutoConfigurer {

  @Bean
  @ConditionalOnMissingBean
  public MybatisPlusInterceptor mybatisPlusInterceptor() {
    PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
    paginationInnerInterceptor.setMaxLimit(500L);

    MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
    mybatisPlusInterceptor.addInnerInterceptor(paginationInnerInterceptor);
    return mybatisPlusInterceptor;
  }

}
