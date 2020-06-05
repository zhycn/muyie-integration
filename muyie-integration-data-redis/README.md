# muyie-integration-data-redis

Redis 是一个开源（BSD许可）的，内存中的数据结构存储系统，它可以用作数据库、缓存和消息中间件。 它支持多种类型的数据结构，如 字符串（strings）， 散列（hashes）， 列表（lists）， 集合（sets）， 有序集合（sorted sets） 与范围查询， bitmaps， hyperloglogs 和 地理空间（geospatial） 索引半径查询。 Redis 内置了 复制（replication），LUA脚本（Lua scripting）， LRU驱动事件（LRU eviction），事务（transactions） 和不同级别的 磁盘持久化（persistence）， 并通过 Redis哨兵（Sentinel）和自动 分区（Cluster）提供高可用性（high availability）。

- http://www.redis.cn/
- https://github.com/antirez/redis
- https://github.com/redisson/redisson

本项目是基本 spring-boot-starter-data-redis 模块搭建的，一是开箱即用，避免重复造轮子；二是增强基础功能，支持 Spring Cache 和分布式锁的集成。

## 快速开始

在你的Spring Boot项目中添加以下依赖：

```
<dependency>
    <groupId>com.github.zhycn</groupId>
    <artifactId>muyie-integration-data-redis</artifactId>
    <version>{latest version}</version>
</dependency>
```

添加Redis的属性配置参数：

```
# Redis数据库索引（默认为0）  
spring.redis.database=0

# Redis服务器地址（单机模式）
spring.redis.host=192.168.10.1

# Redis服务器连接端口（单机模式）
spring.redis.port=6379

# Redis集群环境地址（集群环境）
# spring.redis.cluster.nodes=192.168.10.1:6379,192.168.10.2:6379,192.168.10.3:6379

# Redis服务器连接密码（默认为空）
spring.redis.password=

# 连接池最大连接数（使用负值表示没有限制，默认为8）
spring.redis.pool.max-active=200

# 连接池最大阻塞等待时间（使用负值表示没有限制） 
spring.redis.pool.max-wait=-1

# 连接池中的最大空闲连接（默认为8）
spring.redis.pool.max-idle=8

# 连接池中的最小空闲连接（默认为0）
spring.redis.pool.min-idle=0

# 连接超时时间（毫秒）
spring.redis.timeout=1000
```

## 使用向导

- [Redis分布式锁的正确用法](./docs/redis-lock.md)
- [Spring Cache 接口的用法](https://www.jianshu.com/p/6db623355e11)