# 作业执行快速参考指南

本指南提供使用各种参数和配置执行产品数据ETL作业的快速参考。

## 🚀 快速启动命令

### 基本执行
```bash
# 使用默认设置运行
mvn spring-boot:run

# 运行打包的JAR
java -jar target/product-data-etl-0.0.1-SNAPSHOT.jar
```

### 常见执行场景

#### 1. 自定义输入/输出文件
```bash
# 自定义CSV输入文件
java -jar product-data-etl.jar \
  --batch.input.file=file:/path/to/custom-products.csv

# 自定义输出位置
java -jar product-data-etl.jar \
  --batch.output.file=file:/path/to/custom-report.csv

# 自定义输入和输出
java -jar product-data-etl.jar \
  --batch.input.file=file:/data/products.csv \
  --batch.output.file=file:/reports/sales_report.csv
```

#### 2. 性能调优
```bash
# 大数据集处理
java -Xmx4g -jar product-data-etl.jar \
  --batch.chunk.size=1000 \
  --batch.page.size=1000

# 小数据集详细日志
java -jar product-data-etl.jar \
  --batch.chunk.size=5 \
  --logging.level.com.example.productdataetl=DEBUG
```

#### 3. 错误处理自定义
```bash
# 严格错误处理（快速失败）
java -jar product-data-etl.jar \
  --batch.skip.limit=0 \
  --batch.retry.limit=1

# 宽松错误处理
java -jar product-data-etl.jar \
  --batch.skip.limit=100 \
  --batch.retry.limit=5 \
  --batch.error.log.skipped.items=true
```

#### 4. 业务逻辑自定义
```bash
# 销售报告的不同价格阈值
java -jar product-data-etl.jar \
  --batch.sales.report.price.threshold=75.0

# 自定义日期格式
java -jar product-data-etl.jar \
  --batch.date.format="dd/MM/yyyy HH:mm:ss"
```

## 📋 参数参考

### 文件处理参数

| 参数 | 默认值 | 描述 | 示例 |
|------|--------|------|------|
| `batch.input.file` | classpath:products.csv | 输入CSV文件位置 | `file:/data/products.csv` |
| `batch.output.file` | file:sales_report.csv | 输出报告文件位置 | `file:/reports/sales.csv` |
| `batch.csv.delimiter` | , | CSV字段分隔符 | `;` 或 `\|` |
| `batch.csv.skip.lines` | 1 | 要跳过的标题行数 | `0` 或 `2` |

### 处理参数

| 参数 | 默认值 | 描述 | 示例 |
|------|--------|------|------|
| `batch.chunk.size` | 10 | 每个事务块的项目数 | `100` 或 `1000` |
| `batch.page.size` | 100 | 数据库分页大小 | `500` 或 `1000` |
| `batch.thread.pool.size` | 1 | 核心线程池大小 | `2` 或 `4` |

### 错误处理参数

| 参数 | 默认值 | 描述 | 示例 |
|------|--------|------|------|
| `batch.skip.limit` | 5 | 最大跳过项目数 | `0`（快速失败）或 `100` |
| `batch.retry.limit` | 3 | 最大重试次数 | `1` 或 `10` |
| `batch.retry.initial.delay` | 1000 | 初始重试延迟（毫秒） | `500` 或 `2000` |
| `batch.error.log.skipped.items` | true | 记录跳过的项目 | `false` |

### 业务逻辑参数

| 参数 | 默认值 | 描述 | 示例 |
|------|--------|------|------|
| `batch.sales.report.price.threshold` | 50.0 | 价格过滤阈值 | `25.0` 或 `100.0` |
| `batch.date.format` | yyyy-MM-dd HH:mm:ss | 导入日期格式 | `dd/MM/yyyy` |

## 🔧 环境特定执行

### 开发环境
```bash
java -jar product-data-etl.jar \
  --spring.profiles.active=dev \
  --logging.level.com.example.productdataetl=DEBUG \
  --batch.chunk.size=5
```

### 测试环境
```bash
java -jar product-data-etl.jar \
  --spring.profiles.active=test \
  --batch.input.file=classpath:test-products.csv \
  --batch.chunk.size=2
```

### 生产环境
```bash
java -Xmx2g -jar product-data-etl.jar \
  --spring.profiles.active=prod \
  --batch.chunk.size=1000 \
  --batch.page.size=1000 \
  --logging.level.root=WARN
```

## 🔄 作业重启和恢复

### 重启失败的作业
```bash
# 使用相同参数重启
java -jar product-data-etl.jar \
  --spring.batch.job.names=productEtlJob \
  --restart=true

# 使用不同参数重启
java -jar product-data-etl.jar \
  --spring.batch.job.names=productEtlJob \
  --restart=true \
  --batch.chunk.size=50
```

### 唯一执行的作业参数
```bash
# 添加时间戳以获得唯一作业实例
java -jar product-data-etl.jar \
  --job.parameters="timestamp=$(date +%s)"

# 添加自定义运行标识符
java -jar product-data-etl.jar \
  --job.parameters="runId=manual-$(date +%Y%m%d-%H%M%S)"
```

## 📊 监控和调试

### 调试模式
```bash
# 启用调试日志
java -jar product-data-etl.jar \
  --logging.level.com.example.productdataetl=DEBUG \
  --logging.level.org.springframework.batch=DEBUG

# SQL查询日志
java -jar product-data-etl.jar \
  --spring.jpa.show-sql=true \
  --logging.level.org.hibernate.SQL=DEBUG
```

### 性能监控
```bash
# 启用JMX监控
java -Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=9999 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  -jar product-data-etl.jar

# 启用actuator端点
java -jar product-data-etl.jar \
  --management.endpoints.web.exposure.include=health,metrics,batch
```

## 🚨 故障排除命令

### 文件问题
```bash
# 检查文件权限
ls -la /path/to/input/products.csv

# 使用绝对路径测试
java -jar product-data-etl.jar \
  --batch.input.file=file:/absolute/path/to/products.csv
```

### 内存问题
```bash
# 增加堆大小
java -Xmx4g -jar product-data-etl.jar

# 启用GC日志
java -XX:+PrintGC -XX:+PrintGCDetails \
  -Xloggc:gc.log \
  -jar product-data-etl.jar
```

### 数据库问题
```bash
# 启用H2控制台进行调试
java -jar product-data-etl.jar \
  --spring.h2.console.enabled=true

# 使用外部数据库测试
java -jar product-data-etl.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/testdb \
  --spring.datasource.username=testuser \
  --spring.datasource.password=testpass
```

## 📝 示例执行脚本

### Linux/Mac脚本（run.sh）
```bash
#!/bin/bash

# 设置变量
JAR_FILE="target/product-data-etl-0.0.1-SNAPSHOT.jar"
INPUT_FILE="/data/products.csv"
OUTPUT_FILE="/reports/sales_report_$(date +%Y%m%d).csv"

# 执行作业
java -Xmx2g -jar "$JAR_FILE" \
  --batch.input.file="file:$INPUT_FILE" \
  --batch.output.file="file:$OUTPUT_FILE" \
  --batch.chunk.size=500 \
  --job.parameters="timestamp=$(date +%s)"

echo "作业完成。报告保存到：$OUTPUT_FILE"
```

### Windows脚本（run.bat）
```batch
@echo off

set JAR_FILE=target\product-data-etl-0.0.1-SNAPSHOT.jar
set INPUT_FILE=C:\data\products.csv
set OUTPUT_FILE=C:\reports\sales_report_%date:~-4,4%%date:~-10,2%%date:~-7,2%.csv

java -Xmx2g -jar "%JAR_FILE%" ^
  --batch.input.file="file:%INPUT_FILE%" ^
  --batch.output.file="file:%OUTPUT_FILE%" ^
  --batch.chunk.size=500

echo 作业完成。报告保存到：%OUTPUT_FILE%
```

本快速参考指南提供了在各种场景中有效执行产品数据ETL作业所需的所有基本命令和参数。