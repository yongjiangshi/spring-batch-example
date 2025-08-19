# 配置指南

本文档提供有关产品数据ETL应用程序的所有配置选项、作业执行参数和自定义可能性的详细信息。

## 📋 目录

- [应用程序属性](#应用程序属性)
- [作业执行参数](#作业执行参数)
- [环境特定配置](#环境特定配置)
- [高级配置](#高级配置)
- [性能调优](#性能调优)
- [安全配置](#安全配置)

## 🔧 应用程序属性

### 数据库配置

```properties
# H2数据库设置
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA/Hibernate设置
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
```

**生产数据库示例（PostgreSQL）：**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/productdb
spring.datasource.username=${DB_USERNAME:admin}
spring.datasource.password=${DB_PASSWORD:password}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### 批处理配置

```properties
# 核心批处理设置
spring.batch.job.enabled=false
spring.batch.initialize-schema=always
spring.batch.job.names=productEtlJob

# 处理参数
batch.chunk.size=10
batch.page.size=100
batch.thread.pool.size=1
batch.thread.pool.max.size=5
batch.thread.pool.queue.capacity=25
```

### 文件处理配置

```properties
# 文件位置
batch.input.file=classpath:products.csv
batch.output.file=file:sales_report.csv

# CSV处理设置
batch.csv.delimiter=,
batch.csv.quote.character="
batch.csv.skip.lines=1
```

### 错误处理配置

```properties
# 跳过和重试设置
batch.skip.limit=5
batch.retry.limit=3
batch.error.log.skipped.items=true

# 重试时间
batch.retry.initial.delay=1000
batch.retry.max.delay=10000
batch.retry.multiplier=2.0
```

### 业务逻辑配置

```properties
# 业务规则
batch.sales.report.price.threshold=50.0
batch.date.format=yyyy-MM-dd HH:mm:ss
```

## 🚀 作业执行参数

### 命令行参数

应用程序支持各种命令行参数进行运行时配置：

#### 基本执行
```bash
# 标准执行
java -jar product-data-etl.jar

# 使用特定作业名称
java -jar product-data-etl.jar --spring.batch.job.names=productEtlJob
```

#### 文件配置
```bash
# 自定义输入文件
java -jar product-data-etl.jar --batch.input.file=file:/path/to/input.csv

# 自定义输出文件
java -jar product-data-etl.jar --batch.output.file=file:/path/to/output.csv

# 网络文件位置
java -jar product-data-etl.jar --batch.input.file=ftp://server/input.csv
```

#### 处理参数
```bash
# 自定义块大小
java -jar product-data-etl.jar --batch.chunk.size=100

# 自定义页面大小
java -jar product-data-etl.jar --batch.page.size=500

# 自定义价格阈值
java -jar product-data-etl.jar --batch.sales.report.price.threshold=75.0
```

#### 错误处理参数
```bash
# 自定义跳过限制
java -jar product-data-etl.jar --batch.skip.limit=10

# 自定义重试限制
java -jar product-data-etl.jar --batch.retry.limit=5

# 禁用错误日志记录
java -jar product-data-etl.jar --batch.error.log.skipped.items=false
```

### 作业参数

Spring Batch作业参数可以传递用于作业标识和重启功能：

```bash
# 带时间戳参数的作业
java -jar product-data-etl.jar --job.parameters="timestamp=$(date +%s)"

# 带自定义运行ID的作业
java -jar product-data-etl.jar --job.parameters="runId=manual-001"

# 多个参数
java -jar product-data-etl.jar --job.parameters="timestamp=$(date +%s),environment=production"
```

## 🌍 环境特定配置

### 开发环境

**application-dev.properties：**
```properties
# 开发设置
logging.level.com.example.productdataetl=DEBUG
spring.jpa.show-sql=true
spring.h2.console.enabled=true
batch.chunk.size=5
batch.error.log.skipped.items=true
```

### 测试环境

**application-test.properties：**
```properties
# 测试设置
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
logging.level.org.springframework.batch=WARN
batch.chunk.size=2
batch.skip.limit=1
```

### 生产环境

**application-prod.properties：**
```properties
# 生产设置
logging.level.root=WARN
logging.level.com.example.productdataetl=INFO
spring.jpa.show-sql=false
spring.h2.console.enabled=false
batch.chunk.size=1000
batch.page.size=1000
batch.thread.pool.max.size=10

# 生产数据库
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
```

### 激活
```bash
# 使用特定配置文件运行
java -jar product-data-etl.jar --spring.profiles.active=prod

# 多个配置文件
java -jar product-data-etl.jar --spring.profiles.active=prod,monitoring
```

## 🔧 高级配置

### 自定义ItemReader配置

```properties
# 文件读取器设置
batch.reader.file.encoding=UTF-8
batch.reader.file.strict=true
batch.reader.file.buffer.size=8192

# 数据库读取器设置
batch.reader.db.fetch.size=100
batch.reader.db.timeout=30000
```

### 自定义ItemWriter配置

```properties
# 文件写入器设置
batch.writer.file.encoding=UTF-8
batch.writer.file.append=false
batch.writer.file.force.sync=true

# 数据库写入器设置
batch.writer.db.batch.size=50
batch.writer.db.timeout=30000
```

### 线程池配置

```properties
# 异步处理
batch.async.enabled=false
batch.async.core.pool.size=2
batch.async.max.pool.size=10
batch.async.queue.capacity=100
batch.async.keep.alive.seconds=60
```

### 监控配置

```properties
# JMX监控
spring.jmx.enabled=true
spring.jmx.default-domain=productEtl

# Actuator端点
management.endpoints.web.exposure.include=health,info,metrics,beans,batch
management.endpoint.health.show-details=always
management.metrics.export.simple.enabled=true
```

## ⚡ 性能调优

### 内存配置

```bash
# JVM内存设置
java -Xms512m -Xmx2g -jar product-data-etl.jar

# 垃圾收集调优
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar product-data-etl.jar

# 内存监控
java -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -jar product-data-etl.jar
```

### 数据库性能

```properties
# 连接池设置
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# JPA性能
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
```

### 批处理性能

```properties
# 按数据量优化的块大小
# 小数据集（< 1K）：10-50
batch.chunk.size.small=25

# 中等数据集（1K-10K）：100-500
batch.chunk.size.medium=250

# 大数据集（> 10K）：1000+
batch.chunk.size.large=1000

# 页面大小优化
batch.page.size=500
```

## 🔒 安全配置

### 数据库安全

```properties
# 加密密码
spring.datasource.password=${DB_PASSWORD_ENCRYPTED}

# 连接加密
spring.datasource.url=jdbc:postgresql://localhost:5432/productdb?ssl=true&sslmode=require

# 连接验证
spring.datasource.hikari.connection-test-query=SELECT 1
```

### 文件安全

```properties
# 文件权限
batch.file.permissions=rw-r--r--

# 安全文件位置
batch.input.file=file:/secure/input/products.csv
batch.output.file=file:/secure/output/sales_report.csv

# 文件验证
batch.file.validation.enabled=true
batch.file.validation.max.size=100MB
```

### Actuator安全

```properties
# 安全actuator端点
management.endpoints.web.base-path=/management
management.security.enabled=true
management.endpoints.web.exposure.include=health,info
```

## 📊 监控和指标

### 自定义指标

```properties
# 启用自定义指标
management.metrics.enable.batch=true
management.metrics.enable.jvm=true
management.metrics.enable.system=true

# 指标导出
management.metrics.export.prometheus.enabled=true
management.metrics.export.influx.enabled=false
```

### 健康检查

```properties
# 健康检查配置
management.health.batch.enabled=true
management.health.db.enabled=true
management.health.diskspace.enabled=true
management.health.diskspace.threshold=100MB
```

## 🔄 作业重启配置

### 重启参数

```properties
# 作业重启设置
spring.batch.job.restart.enabled=true
batch.job.restart.allow.start.if.complete=false

# 步骤重启设置
batch.step.restart.allow=true
batch.step.restart.limit=3
```

### 重启命令

```bash
# 重启失败的作业
java -jar product-data-etl.jar --spring.batch.job.names=productEtlJob --restart=true

# 从特定步骤重启
java -jar product-data-etl.jar --restart.from.step=step2_generateReportFromDb
```

## 🧪 测试配置

### 测试属性

```properties
# 测试特定设置
spring.test.database.replace=none
spring.batch.job.enabled=true
batch.test.chunk.size=2
batch.test.skip.limit=1
logging.level.org.springframework.batch.test=DEBUG
```

### 集成测试配置

```properties
# 集成测试数据库
spring.datasource.url=jdbc:h2:mem:integrationtest;DB_CLOSE_DELAY=-1
spring.sql.init.mode=always
spring.batch.initialize-schema=always
```

本配置指南提供了根据您的特定要求自定义和调优产品数据ETL应用程序的所有可用选项的全面覆盖。