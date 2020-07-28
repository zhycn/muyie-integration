# Flyway - 数据库版本迁移工具

Flyway 和 Liquibase 都是 Java 项目中常用的 DB migration 工具。从使用简便性看，Flyway 比 Liquibase 更简单；从 github 的 star 数量看，Flyway 更受欢迎。

官网地址：https://flywaydb.org

对于 SpringBoot 项目的开发者来说，其实不需要专门安装 Flyway 命令行工具和 maven 插件，SpringBoot 启动就会自动执行 DB migrate 操作。对于其他的 Flyway 操作，就需要使用命令行工具或 maven 插件了。

Flyway 提供命令行工具，常用的命令包括：

- **Clean**: 删除所有创建的数据库对象，包括用户、表、视图等，注意不要在生产库上执行 clean 操作。
- **Migrate**：对数据库依次应用版本更改。
- **Info**：获取目前数据库的状态。那些迁移已经完成，那些迁移待完成，所有迁移的执行时间以及结果。
- **Validate**：验证已 Apply 的脚本是否有变更，Flyway 的 Migration 默认先做 Validate。
- **Baseline**：根据现有的数据库结构生成一个基准迁移脚本。
- **Repair**：修复命令尽量不要使用，修复场景有：1. 移除失败的 migration 记录；2. 已经应用的 SQL 脚本被修改，我们想重新应用该 SQL 脚本。
- **Undo**：撤销迁移的脚本，需要专业版才能使用。

## 快速开始

**第1步：在你的 Spring Boot 项目中添加依赖**

```
<dependency>
  <groupId>org.flywaydb</groupId>
  <artifactId>flyway-core</artifactId>
</dependency>
```

或者使用本项目：

```
<dependency>
  <groupId>com.github.zhycn</groupId>
  <artifactId>muyie-integration-flyway</artifactId>
  <version>{latest version}</version>
</dependency>
```

**第2步：设置 flyway 属性**

```
# flyway 的 clean 命令会删除指定 schema 下的所有 table，杀伤力太大了，应该禁掉
# 默认值：false
spring.flyway.cleanDisabled=true

# 启用或禁用 flyway
# 默认值：true
spring.flyway.enabled=true

# 设定 SQL 脚本的目录，多个路径使用逗号分隔，比如取值为：classpath:db/migration,filesystem:/sql-migrations
# 默认值：classpath:db/migration
spring.flyway.locations=classpath:db/migration

# 如果指定 schema 包含了其他表，但没有 flyway_schema_history 表的话，在执行 flyway migrate 命令之前，必须先执行 flyway baseline 命令。
# 设置 spring.flyway.baseline-on-migrate 为 true 后，flyway 将在需要 baseline 的时候，自动执行一次 baseline。 
# 默认值：false
spring.flyway.baselineOnMigrate=true

# 指定 baseline 的版本号，缺省值为 1，低于该版本号的 SQL 文件，migrate 的时候被忽略
# 默认值：1
# spring.flyway.baselineVersion=1 

# Encoding of SQL migrations (default: UTF-8)
# spring.flyway.encoding=UTF-8

# 设定 flyway 的 metadata 表名，缺省为 flyway_schema_history
# 默认值：flyway_schema_history
# spring.flyway.table=flyway_schema_history_myapp
```

**第3步：创建SQL版本管理目录**

默认的路径是：classpath:db/migration

SQL脚本的命名规范：V+<版本号>+__+<脚本用途说明>.sql。版本号可以是点号或下划线分割的数字或直接用数字，并且数字必须是递增。

- 已经交付的版本，不要手动删除或变更 flyway 和数据表中的任何记录信息。
- 使用 Flyway 时，要重点注意SQL的正确性，避免执行异常。
- 使用 Flyway 时，需要较大的数据库操作权限，尤其要注意风险。

## 参考

- [SpringBoot系列: 使用 flyway 管理数据库版本](https://www.cnblogs.com/harrychinese/p/springboot_flyway.html)
- [Flyway：数据库版本迁移工具的介绍](https://www.jianshu.com/p/b321dafdfe83)
- [SpringBoot 集成 Flyway](https://www.jianshu.com/p/779c2b4c053c)
- [Flyway使用指南](https://www.cnblogs.com/417xiaoliu/p/11124080.html)