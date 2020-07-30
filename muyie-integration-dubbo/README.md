# muyie-integration-dubbo

Apache Dubbo™ 是一款高性能Java RPC框架。

http://dubbo.apache.org/zh-cn/index.html

本项目已集成DUBBO配置，提供常用的dubbo和rest协议支持。

## 快速开始

**1. 在你的 Spring Boot 项目中添加以下依赖：**

```
<dependency>
  <groupId>com.github.zhycn</groupId>
  <artifactId>muyie-integration-dubbo</artifactId>
  <version>{latest version}</version>
</dependency>
```

**2. 添加服务提供者配置：**

```
## Dubbo 服务提供者配置
dubbo.application.name=dubbo-provider
dubbo.registry.protocol=zookeeper
dubbo.registry.address=zookeeper://10.177.84.73:2181?backup=10.177.84.74:2181,10.177.84.75:2181
dubbo.registry.timeout=60000
dubbo.consumer.timeout=10000
dubbo.protocols.rest.name=rest
dubbo.protocols.rest.server=netty
dubbo.protocols.rest.port=28001
dubbo.protocols.rest.contextpath=services
dubbo.protocols.dubbo.name=dubbo
dubbo.protocols.dubbo.port=28002
```

**3. 添加服务消费者配置：**

```
dubbo.application.name=dubbo-consumer
dubbo.registry.protocol=zookeeper
dubbo.registry.address=zookeeper://10.177.84.73:2181?backup=10.177.84.74:2181,10.177.84.75:2181
dubbo.registry.timeout=60000
dubbo.consumer.timeout=10000
# 协议类型可以注解中指定
dubbo.protocols.rest.name=rest
dubbo.protocols.dubbo.name=dubbo
```

## 配置

```
@SpringBootApplication
@EnableDubbo(scanBasePackages = "com.xxx") // 配置扫描包
public class Application {

}
```

## 服务接口定义

作为DUBBO服务，需要提供一个抽象的API包，供服务提供者和消费者使用。

```
public interface IHelloService {
  String sayHello(String name);
}
```

如果使用rest协议，则配置相应的注解：

```
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("hello")
@Consumes({MediaType.APPLICATION_JSON,MediaType.TEXT_XML})
@Produces({"application/json;charset=UTF-8"})
public interface IHelloService {

  @GET
  @Path("sayHello")
  String sayHello(@QueryParam("name") String name);
}
```

使用rest协议时，需要添加注解依赖包：

```
<dependency>
  <groupId>javax.ws.rs</groupId>
  <artifactId>javax.ws.rs-api</artifactId>
  <version>2.0</version>
</dependency>
```

注意：为了防止因接口参数变动，带来的序列化问题。以JSON为例，可以实体类上添加序列化注解及依赖包：

```
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-core</artifactId>
</dependency>
```

通过序列化注解来解决版本兼容性问题：

```
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressDTO implements Serializable {

}
```

## 服务提供者

服务生产者使用@DubboService注解配置，能在生产者上配置的参数，应尽量在生产者上配置，以降低消费者的使用成本。

```
@DubboService(interfaceClass = HelloService.class, protocol = {"rest",
    "dubbo"}, version = "1.0", retries = -1)
public class HelloServiceImpl implements HelloService {

}
```

## 服务消费者

服务消费者使用@DubboReference注解配置。

```
@DubboReference(protocol = "rest", version = "1.0", check = false)
private HelloService helloService;
```