# muyie-integration-apollo

Apollo（阿波罗）是携程框架部门研发的分布式配置中心，能够集中化管理应用不同环境、不同集群的配置，配置修改后能够实时推送到应用端，并且具备规范的权限、流程治理等特性，适用于微服务配置管理场景。

https://github.com/ctripcorp/apollo

本项目是基于 Spring Boot 集成的 Apollo Client，解决了在 Spring Boot @ConfigurationProperties 使用时的配置同步问题，服务端搭建可参考官方文档。

## 快速开始

在你的 Spring Boot 项目中使用 Apollo Client 可添加以下 Maven 依赖：

```
  <dependency>
    <groupId>com.github.zhycn</groupId>
    <artifactId>muyie-integration-apollo</artifactId>
    <version>latest version</version>
  </dependency>
```

在 Spring Boot 中集成使用很简单，只需要在 application.properties/bootstrap.properties 中按照如下样例配置即可：

```
# 开始 Apollo 配置
apollo.bootstrap.enabled=true

# 将 Apollo 配置加载提到初始化日志系统之前（可选）
apollo.bootstrap.eagerLoad.enabled=true

# 指定加载的命名空间，默认会加载 application
apollo.bootstrap.namespaces=application,FX.apollo,application.yml

# 指定配置环境，忽略大小写
apollo.profile.actived=dev

# 指定 app.id
app.id=100004458

# 指定配置服务器地址
apollo.meta=http://localhost:8080
```

更多配置请参考官方文档：[Java 客户端使用指南](https://github.com/ctripcorp/apollo/wiki/Java%E5%AE%A2%E6%88%B7%E7%AB%AF%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97)

### 本地缓存路径

Apollo 客户端会把从服务端获取到的配置在本地文件系统缓存一份，用于在遇到服务不可用，或网络不通的时候，依然能从本地恢复配置，不影响应用正常运行。

本地缓存路径默认位于以下路径，所以请确保 /opt/data 或 C:\opt\data\ 目录存在，且应用有读写权限。

- Mac/Linux: /opt/data/{appId}/config-cache
- Windows: C:\opt\data\{appId}\config-cache

本地配置文件会以下面的文件名格式放置于本地缓存路径下：

```
{appId}+{cluster}+{namespace}.properties
```

- appId 就是应用自己的 appId（可规范命名规则），如 100004458
- cluster 就是应用使用的集群，一般在本地模式下没有做过配置的话，就是 default
- namespace 就是应用使用的配置 namespace，一般是 application

### Spring Placeholder 的使用

Spring 应用通常会使用 Placeholder 来注入配置，使用的格式形如 ${someKey:someDefaultValue}，如 ${timeout:100}。冒号前面的是key，冒号后面的是默认值。

建议在实际使用时尽量给出默认值，以免由于 key 没有定义导致运行时错误。

从 v0.10.0 开始的版本支持 placeholder 在运行时自动更新。

假设有一个 TestJavaConfigBean，通过 Java Config 的方式还可以使用 @Value 的方式注入：

```
public class TestJavaConfigBean {

  @Value("${timeout:100}")
  private int timeout;
  private int batch;
 
  @Value("${batch:200}")
  public void setBatch(int batch) {
    this.batch = batch;
  }
 
  public int getTimeout() {
    return timeout;
  }
 
  public int getBatch() {
    return batch;
  }
}
```

在 Configuration 类中按照下面的方式使用（假设应用默认的 application namespace 中有 timeout 和 batch 的配置项）：

```
@Configuration
@EnableApolloConfig
public class AppConfig {

  @Bean
  public TestJavaConfigBean javaConfigBean() {
    return new TestJavaConfigBean();
  }

}
```

Spring Boot 提供了 @ConfigurationProperties 把配置注入到 bean 对象中。

Apollo 也支持这种方式，下面的例子会把 redis.cache.expireSeconds 和 redis.cache.commandTimeout 分别注入到 SampleRedisConfig 的 expireSeconds 和 commandTimeout 字段中。

```
@ConfigurationProperties(prefix = "redis.cache")
public class SampleRedisConfig {

  private int expireSeconds;
  private int commandTimeout;

  public void setExpireSeconds(int expireSeconds) {
    this.expireSeconds = expireSeconds;
  }

  public void setCommandTimeout(int commandTimeout) {
    this.commandTimeout = commandTimeout;
  }
}
```

在 Configuration 类中按照下面的方式使用（假设应用默认的 application namespace 中有 redis.cache.expireSeconds 和 redis.cache.commandTimeout 的配置项）：

```
@Configuration
@EnableApolloConfig
public class AppConfig {

  @Bean
  public SampleRedisConfig sampleRedisConfig() {
    return new SampleRedisConfig();
  }
}
```

在使用 Spring Boot 提供的 @ConfigurationProperties 时，需要增加配置才能实现自动更新。

本项目重点解决了在 Spring Boot @ConfigurationProperties 使用时的配置同步问题，代码如下：

```
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
      log.info("Apollo ConfigChange - {}", configChange);
    });

    // 更新相应的 Bean 的属性值，主要是存在 @ConfigurationProperties 注解的 Bean
    SpringContextHolder.getApplicationContext()
        .publishEvent(new EnvironmentChangeEvent(event.changedKeys()));
  }

}
```
