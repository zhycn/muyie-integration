# muyie-integration-dubbo

Apache Dubbo™ 是一款高性能Java RPC框架。

http://dubbo.apache.org/zh-cn/index.html


## 快速开始

**1. 在你的 Spring Boot 项目中添加以下依赖：**

```
<dependency>
    <groupId>com.github.zhycn</groupId>
    <artifactId>muyie-integration-dubbo</artifactId>
    <version>{latest version}</version>
</dependency>
```

2. 添加配置参数：

```
## Dubbo 服务提供者配置
dubbo.protocol.name = rest
dubbo.protocol.port = 20081

dubbo.protocol.server = netty
dubbo.protocol.contextpath = services
#当前服务/应用的名字
dubbo.application.name = mfbp-tesseract
#注册中心的协议和地址
dubbo.registry.protocol = zookeeper
dubbo.registry.address = zookeeper://10.177.84.73:2181?backup=10.177.84.74:2181
#连接监控中心
##dubbo.monitor.protocol = registry
dubbo.consumer.timeout = 1000
```