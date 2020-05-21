# Redis 分布式锁的正确用法

说起分布式的概念，首当其冲就是CAP理论，即满足一致性（Consistency）、可用性（Availability）和分区容错性（Partition tolerance）。但是CAP理论告诉我们，任何系统只能满足其中两个，所以都要去做取舍。那么人们常说的一般都是，需要牺牲一致性来保证系统的高可用性，只要保证系统的最终一致性，并且允许的时间差值能够被接受就行。例如订单系统，用户端的一致性需要保证强一致性，但是对于后台或者商家来说的话，这个订单的状态只要保证最终一致性就行，时间差值在可接受范围内即可。

单机环境下我们通过一些简单的方式就可实现锁。在实际应用中，往往部署的是分布式系统，这就要求我们不得不使用分布式锁来保障业务一致性和安全性。

Redis 设计为单线程模式，采用队列模式半并发访问变成串行访问，且多客户端对 redis 的连接并不存在竞争关系。Redis 提供了一些命令可以方便实现分布式锁机制，例如：SETNX，GETSET 等。目前来看网上大部分基于 Redis 锁的实现都非常不严谨，封装性差且不易使用。下面我们介绍两种更好的基于 redis 分布式锁的实现，能够轻松集成到 Spring Boot 项目中。

## 1. 基于 Spring Integration 项目的 Redis 分布式锁的实现

Spring Integration 项目是一个企业集成模式的实现，它的定位是一个轻量级的 ESB，尽管它做了很多 ESB 不做的事情。顺便说一下，Spring Cloud Stream 的底层也是 Spring Integration。

Spring Integration提供的全局锁目前为如下存储提供了实现：

- Gemfire
- JDBC
- Redis
- Zookeeper

本文我们只讨论基于 Redis 的实现。

**第一步：添加 Maven 依赖**

```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-integration</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.integration</groupId>
  <artifactId>spring-integration-redis</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**第二步：添加Redis配置（单机和集群模式均可支持）**

```
spring:
  redis:
  port: 6379
  host: localhost
```

**第三步：注册 Spring Bean（有兴趣的同学可以看一看它的底层接口实现）**

```
@Configuration
public class RedisLockConfiguration {

  @Bean
  public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
    return new RedisLockRegistry(redisConnectionFactory, "muyie-rlock"); // 自定义registryKey
  }

}
```

**第四步：编写测试**

```
@GetMapping("test")
public void test() throws InterruptedException {
  Lock lock = redisLockRegistry.obtain("lock");
  try {
    // 尝试等待3秒，如果没有拿到锁就抛出异常
    boolean b1 = lock.tryLock(3, TimeUnit.SECONDS);
    log.info("b1 is : {}", b1);

    TimeUnit.SECONDS.sleep(5);

    boolean b2 = lock.tryLock(3, TimeUnit.SECONDS);
    log.info("b2 is : {}", b2);
  } catch (Exception e) {
    e.printStackTrace(); // 异常处理
  } finally {
    lock.unlock();
  }

}
```

**第五步：执行测试**

启动实例1，访问http://localhost:8080/test，会看到类似如下的日志：

```
2019-03-15 00:48:38.948 INFO 21893 --- [nio-8080-exec-1] c.itmuch.lock.SpringBootLockApplication : b1 is : true
2019-03-15 00:48:43.958 INFO 21893 --- [nio-8080-exec-1] c.itmuch.lock.SpringBootLockApplication : b2 is : true


启动实例2，快速访问实例的test地址，会看到类似如下日志：
2019-03-15 00:50:08.222 INFO 21898 --- [nio-8081-exec-3] c.itmuch.lock.SpringBootLockApplication : b1 is : false
2019-03-15 00:50:13.233 INFO 21898 --- [nio-8081-exec-3] c.itmuch.lock.SpringBootLockApplication : b2 is : true
2019-03-15 00:50:13.252 ERROR 21898 --- [nio-8081-exec-3] o.a.c.c.C.[.[.[/].[dispatcherServlet] : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is java.lang.IllegalStateException: You do not own lock at spring-cloud:lock] with root cause
 
java.lang.IllegalStateException: You do not own lock at spring-cloud:lock
 at org.springframework.integration.redis.util.RedisLockRegistry$RedisLock.unlock(RedisLockRegistry.java:300) ~[spring-integration-redis-5.1.3.RELEASE.jar:5.1.3.RELEASE]
 at com.itmuch.lock.SpringBootLockApplication.test(SpringBootLockApplication.java:33) ~[classes/:na]
 at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.8.0_201]
 at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[na:1.8.0_201]
 at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[na:1.8.0_201]
 at java.lang.reflect.Method.invoke(Method.java:498) ~[na:1.8.0_201]
 at org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:189) ~[spring-web-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 at org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:138) ~[spring-web-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 at org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:102) ~[spring-webmvc-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:895) ~[spring-webmvc-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 at org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:800) ~[spring-webmvc-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 at org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:87) ~[spring-webmvc-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1038) ~[spring-webmvc-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 at org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:942) ~[spring-webmvc-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 at org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1005) ~[spring-webmvc-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 at org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:897) ~[spring-webmvc-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 at javax.servlet.http.HttpServlet.service(HttpServlet.java:634) ~[tomcat-embed-core-9.0.16.jar:9.0.16]
 at org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:882) ~[spring-webmvc-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 at javax.servlet.http.HttpServlet.service(HttpServlet.java:741) ~[tomcat-embed-core-9.0.16.jar:9.0.16]
 at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:231) ~[tomcat-embed-core-9.0.16.jar:9.0.16]
 at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.16.jar:9.0.16]
 at org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:53) ~[tomcat-embed-websocket-9.0.16.jar:9.0.16]
 at org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:193) ~[tomcat-embed-core-9.0.16.jar:9.0.16]
 at org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:166) ~[tomcat-embed-core-9.0.16.jar:9.0.16]
 at org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:99) ~[spring-web-5.1.5.RELEASE.jar:5.1.5.RELEASE]
 at org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:107) ~[spring-web-5.1.5.RELEASE.jar:5.1.5.RELEASE]
```

说明在没拿到锁的情况下，会抛出异常，证明锁是存在的。

## 2. 基于 Redisson 项目的 Redis 分布式锁的实现

Redisson 是一个在 Redis 的基础上实现的 Java 驻内存数据网格（In-Memory Data Grid）。它不仅提供了一系列的分布式的 Java 常用对象，还提供了许多分布式服务。其中包括(BitSet, Set, Multimap, SortedSet, Map, List, Queue, BlockingQueue, Deque, BlockingDeque, Semaphore, Lock, AtomicLong, CountDownLatch, Publish / Subscribe, Bloom filter, Remote service, Spring cache, Executor service, Live Object service, Scheduler service) Redisson 提供了使用 Redis 的最简单和最便捷的方法。Redisson的宗旨是促进使用者对Redis的关注分离（Separation of Concern），从而让使用者能够将精力更集中地放在处理业务逻辑上。

熟悉 Redis 的用户，应该对 Redisson 并不陌生。接下来看看基于 Redisson 项目的 Redis 分布式锁的实现。

**第一步：添加Maven依赖**

```
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
  <groupId>org.redisson</groupId>
  <artifactId>redisson-spring-boot-starter</artifactId>
</dependency>
```

这里我们使用 Redisson 官方提供的 redisson-spring-boot-starter 项目实现自动配置。要注意的是与 Spring Boot 的版本要匹配，请查看文档：https://github.com/redisson/redisson/tree/master/redisson-spring-boot-starter。

**第二步：添加Redis配置（单机和集群模式均可支持）**

```
spring:
  redis:
  port: 6379
  host: localhost
```

**第三步：编写测试**

```
@GetMapping("test")
public void test() throws InterruptedException {
  Lock lock = redisLockRegistry.obtain("lock");
  try {
    // 尝试等待3秒，如果没有拿到锁就抛出异常
    boolean b1 = lock.tryLock(3, TimeUnit.SECONDS);
    log.info("b1 is : {}", b1);

    TimeUnit.SECONDS.sleep(5);

    boolean b2 = lock.tryLock(3, TimeUnit.SECONDS);
    log.info("b2 is : {}", b2);
  } catch (Exception e) {
    e.printStackTrace(); // 异常处理
  } finally {
    lock.unlock();
  }

}
```

**第四步：执行测试**

可以参考上文的测试步骤，执行结果是一样的。

## 参考

- [Spring Boot 2 实现分布式锁——这才是实现分布式锁的正确姿势！](http://www.itmuch.com/spring-boot/global-lock/)
- [Redisson](https://github.com/redisson/redisson)