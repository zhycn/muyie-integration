package org.muyie.integration.dubbo;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableDubbo // 扫描所有的包，从中找出dubbo的@Service标注的类
public class DubboAutoConfiguration {

}