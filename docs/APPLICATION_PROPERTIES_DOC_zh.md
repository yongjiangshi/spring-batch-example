# application.properties 技术详解

## 1. 简介

`application.properties` 文件是 Spring Boot 应用的核心配置文件。它提供了一个集中管理所有可配置参数的地方，从数据库连接到批处理作业的行为，再到日志记录级别。通过外部化配置，我们可以在不修改任何Java代码的情况下，轻松地调整、部署和维护应用程序。

本文档将逐一解析本项目中使用的所有配置项。

---

## 2. H2 数据库配置

此部分配置了用于开发和测试的H2内存数据库。

| 属性键 | 示例值 | 描述 |
| :--- | :--- | :--- |
| `spring.datasource.url` | `jdbc:h2:mem:testdb` | H2数据库的JDBC连接URL。`mem:testdb` 表示这是一个名为`testdb`的内存数据库，应用停止后数据将丢失。 |
| `spring.datasource.driverClassName` | `org.h2.Driver` | H2数据库的JDBC驱动类。 |
| `spring.datasource.username` | `sa` | 数据库连接用户名。 |
| `spring.datasource.password` | `password` | 数据库连接密码。 |
| `spring.h2.console.enabled` | `true` | 启用H2数据库的Web控制台。 |
| `spring.h2.console.path` | `/h2-console` | H2控制台的访问路径，可通过 `http://localhost:8080/h2-console` 访问。 |

---

## 3. JPA/Hibernate 配置

此部分控制JPA（Java Persistence API）及其实现者Hibernate的行为。

| 属性键 | 示例值 | 描述 |
| :--- | :--- | :--- |
| `spring.jpa.database-platform` | `org.hibernate.dialect.H2Dialect` | 指定Hibernate应使用的数据库方言，以生成针对H2优化的SQL。 |
| `spring.jpa.hibernate.ddl-auto` | `none` | 控制Hibernate的数据库表结构生成策略。`none`是生产环境的最佳实践，表示让Hibernate不自动创建或修改表，而是依赖于`schema.sql`等脚本。 |
| `spring.jpa.show-sql` | `true` | 在控制台打印Hibernate生成的SQL语句，便于调试。 |
| `spring.jpa.properties.hibernate.format_sql` | `true` | 将打印的SQL语句进行格式化，提高可读性。 |
| `spring.sql.init.mode` | `always` | 设置数据库初始化模式。`always`表示每次应用启动时都执行SQL脚本。 |
| `spring.sql.init.schema-locations` | `classpath:schema.sql` | 指定用于初始化数据库表结构的SQL脚本位置。`classpath:`表示从项目的资源路径下查找。 |

---

## 4. Spring Batch 配置

此部分专门用于配置Spring Batch框架的行为。

| 属性键 | 示例值 | 描述 |
| :--- | :--- | :--- |
| `spring.batch.job.enabled` | `false` | 禁止Spring Batch在应用启动时自动运行所有已定义的Job。本项目中，我们通过`CommandLineRunner`来精确控制Job的启动时机。 |
| `spring.batch.initialize-schema` | `always` | 控制Spring Batch元数据表的初始化。`always`确保每次启动都检查并创建`BATCH_*`系列表，用于存储作业执行状态。 |
| `spring.batch.job.names` | `productEtlJob` | （可选）用于从命令行触发特定作业的名称。 |

---

## 5. 日志记录配置

此部分精细地控制了不同代码路径下的日志输出级别。

| 属性键 | 示例值 | 描述 |
| :--- | :--- | :--- |
| `logging.level.root` | `INFO` | 设置日志系统的根级别为`INFO`。所有未明确指定的logger都将继承此级别。 |
| `logging.level.org.springframework.batch` | `INFO` | 将Spring Batch框架本身的日志级别设为`INFO`，以观察核心事件。 |
| `logging.level.com.example.productdataetl` | `DEBUG` | 将我们自己应用代码的日志级别设为`DEBUG`，以获得详细的调试信息。 |
| `logging.level.com.example.productdataetl.listener` | `INFO` | 将监听器的日志级别设为`INFO`，使其在标准运行模式下清晰地报告作业和步骤的生命周期事件。 |
| `logging.level.com.example.productdataetl.config.CustomSkipPolicy` | `WARN` | 将跳过策略的日志级别设为`WARN`，仅在发生跳过时记录警告信息。 |
| `logging.level.org.springframework.retry` | `DEBUG` | 开启Spring Retry框架的`DEBUG`日志，以观察详细的重试过程。 |
| `logging.level.org.hibernate.SQL` | `DEBUG` | 让Hibernate打印执行的SQL语句（等同于`spring.jpa.show-sql=true`）。 |
| `logging.level.org.hibernate.type.descriptor.sql.BasicBinder` | `TRACE` | 设为`TRACE`级别可以查看到绑定到SQL预编译语句（PreparedStatement）中的具体参数值，是SQL调试的利器。 |

---

## 6. 文件与批处理配置

这些是自定义的业务和性能相关参数，通过`@Value`注解注入到Java代码中。

| 属性键 | 示例值 | 描述 |
| :--- | :--- | :--- |
| `batch.input.file` | `classpath:products.csv` | 定义输入CSV文件的位置。 |
| `batch.output.file` | `file:sales_report.csv` | 定义输出报告文件的位置。`file:`表示相对于项目根目录的文件系统路径。 |
| `batch.csv.skip.lines` | `1` | 读取CSV时跳过第一行（通常是表头）。 |
| `batch.chunk.size` | `10` | 定义批处理的“块大小”，即每次事务中处理的记录数。这是影响性能和内存占用的关键参数。 |
| `batch.page.size` | `100` | `JpaPagingItemReader`从数据库分页读取数据时，每一页的大小。 |
| `batch.skip.limit` | `5` | 在整个Step执行过程中，允许跳过的最大记录数。超过此限制，Step将失败。 |
| `batch.retry.limit` | `3` | 对于可重试的瞬时异常，允许的最大重试次数。 |

---

## 7. 监控与管理配置

此部分配置了Spring Boot Actuator和JMX，用于应用的监控和管理。

| 属性键 | 示例值 | 描述 |
| :--- | :--- | :--- |
| `spring.jmx.enabled` | `true` | 启用Java管理扩展（JMX），允许通过JMX客户端（如JConsole）监控和管理应用。 |
| `management.endpoints.web.exposure.include` | `health,info,metrics,beans` | 通过HTTP暴露Actuator的端点。`health`检查健康状况，`info`显示应用信息，`metrics`提供度量指标，`beans`显示所有Spring Bean。 |
| `management.endpoint.health.show-details` | `always` | 在访问`/actuator/health`端点时，总是显示详细的健康信息。 |
| `info.app.name` | `Product Data ETL` | 自定义应用信息，会显示在`/actuator/info`端点。 |
