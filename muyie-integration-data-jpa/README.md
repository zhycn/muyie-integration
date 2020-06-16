# muyie-integration-data-jpa

本项目是基于 Spring Boot Data JPA 实现，使用 Alibaba Druid 连接池。

## 快速开始

**1. 在你的 Spring Boot 项目中添加以下依赖：**

```
<dependency>
    <groupId>com.github.zhycn</groupId>
    <artifactId>muyie-integration-data-jpa</artifactId>
    <version>{latest version}</version>
</dependency>
```

如果使用 MySQL数据库，则添加以下驱动（其他数据库请添加相应的驱动）：

```
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
```

**2. 数据库连接池配置：**

```
## Spring DataSource Configuration
spring.datasource.druid.url=jdbc:mysql://localhost:3306/db_name?serverTimezone=UTC&useUnicode=true&characterEncoding=utf-8&autoReconnect=true&autoReconnectForPools=true&noAccessToProcedureBodies=true&allowMultiQueries=true&zeroDateTimeBehavior=convertToNull
spring.datasource.druid.username=
spring.datasource.druid.password=
spring.datasource.druid.initial-size=8
spring.datasource.druid.min-idle=8
spring.datasource.druid.max-active=64
spring.datasource.druid.max-wait=10000
spring.datasource.druid.validation-query=select 'x'
spring.datasource.druid.validation-query-timeout=10
spring.datasource.druid.test-on-borrow=true
spring.datasource.druid.test-on-return=false
spring.datasource.druid.test-while-idle=false
spring.datasource.druid.time-between-eviction-runs-millis=60000
spring.datasource.druid.min-evictable-idle-time-millis=300000
spring.datasource.druid.keep-alive=true
```

更多数据库连接池配置可参考：https://github.com/alibaba/druid/druid-spring-boot-starter

**3. 开启 JPA 配置：**

```
@Configuration
@EnableTransactionManagement // 开启事务配置，只能配置一次（muyie-framwork框架中已提供）
@EnableJpaRepositories(basePackages = "") // 指定仓库包名
@EntityScan(basePackages = "") // 指定实体类包名
public class AppConfiguration {

}
```
