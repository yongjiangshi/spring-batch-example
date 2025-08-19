# 产品数据ETL - Spring Batch应用程序

一个全面的Spring Batch ETL（提取、转换、加载）管道，展示了高级批处理功能。该应用程序通过多步骤工作流处理产品数据：将CSV数据导入数据库，然后生成过滤的销售报告。

## 🚀 功能特性

- **多步骤批处理**：具有适当排序的两步ETL管道
- **文件到数据库处理**：带有数据验证和转换的CSV导入
- **数据库到文件处理**：使用自定义业务逻辑的过滤报告生成
- **强大的错误处理**：跳过策略、重试机制和全面的日志记录
- **Spring Boot集成**：具有自动配置的现代Spring Boot应用程序
- **内存数据库**：H2数据库，便于开发和测试
- **全面测试**：具有高覆盖率的单元和集成测试

## 📋 先决条件

- Java 11或更高版本
- Maven 3.6+或Gradle 6+
- IDE（推荐IntelliJ IDEA、Eclipse或VS Code）

## 🛠️ 设置说明

### 1. 克隆和构建

```bash
# 克隆仓库
git clone <repository-url>
cd product-data-etl

# 构建项目
mvn clean compile

# 运行测试
mvn test

# 打包应用程序
mvn package
```

### 2. 数据库设置

应用程序使用自动配置的H2内存数据库。无需额外设置。

**H2控制台访问：**
- URL：http://localhost:8080/h2-console
- JDBC URL：`jdbc:h2:mem:testdb`
- 用户名：`sa`
- 密码：`password`

### 3. 输入数据准备

将您的CSV文件放置在`src/main/resources/products.csv`，格式如下：

```csv
id,name,description,price
1,笔记本电脑,高性能笔记本电脑,999.99
2,鼠标,无线鼠标,25.50
3,键盘,机械键盘,75.00
```

## 🏃‍♂️ 运行应用程序

### 标准执行

```bash
# 运行完整的ETL管道
mvn spring-boot:run

# 或运行JAR文件
java -jar target/product-data-etl-1.0.0.jar
```

### 自定义配置

```bash
# 使用自定义输入文件运行
java -jar target/product-data-etl-1.0.0.jar --batch.input.file=file:/path/to/custom.csv

# 使用自定义输出位置运行
java -jar target/product-data-etl-1.0.0.jar --batch.output.file=file:/path/to/custom_report.csv

# 使用自定义块大小运行
java -jar target/product-data-etl-1.0.0.jar --batch.chunk.size=50
```

### 开发模式

```bash
# 使用调试日志运行
java -jar target/product-data-etl-1.0.0.jar --logging.level.com.example.productdataetl=DEBUG

# 启用SQL日志运行
java -jar target/product-data-etl-1.0.0.jar --spring.jpa.show-sql=true
```

## 📊 作业执行流程

### 步骤1：CSV到数据库导入
1. **读取**：使用`FlatFileItemReader`从CSV文件读取产品
2. **处理**：添加导入时间戳并验证数据
3. **写入**：使用`JpaItemWriter`持久化到PRODUCTS表

### 步骤2：数据库到报告生成
1. **读取**：使用`JpaPagingItemReader`从数据库读取产品
2. **处理**：过滤价格>50元的产品并转换为报告格式
3. **写入**：使用`FlatFileItemWriter`生成销售报告CSV

## 📁 项目结构

```
src/
├── main/
│   ├── java/com/example/productdataetl/
│   │   ├── config/           # 批处理配置类
│   │   ├── dto/              # 数据传输对象
│   │   ├── exception/        # 自定义异常
│   │   ├── listener/         # 作业和步骤监听器
│   │   ├── model/            # JPA实体
│   │   ├── processor/        # 项目处理器
│   │   ├── reader/           # 项目读取器
│   │   └── writer/           # 项目写入器
│   └── resources/
│       ├── application.properties  # 配置
│       ├── schema.sql             # 数据库模式
│       └── products.csv           # 示例输入数据
└── test/
    ├── java/                 # 单元和集成测试
    └── resources/
        └── test-products.csv # 测试数据
```

## ⚙️ 配置选项

### 核心批处理设置

| 属性 | 默认值 | 描述 |
|------|--------|------|
| `batch.chunk.size` | 10 | 每个事务处理的项目数 |
| `batch.page.size` | 100 | 数据库分页大小 |
| `batch.skip.limit` | 5 | 作业失败前最大跳过项目数 |
| `batch.retry.limit` | 3 | 错误的最大重试次数 |

### 文件处理

| 属性 | 默认值 | 描述 |
|------|--------|------|
| `batch.input.file` | classpath:products.csv | 输入CSV文件位置 |
| `batch.output.file` | file:sales_report.csv | 输出报告文件位置 |
| `batch.csv.delimiter` | , | CSV字段分隔符 |
| `batch.csv.skip.lines` | 1 | 要跳过的标题行数 |

### 业务逻辑

| 属性 | 默认值 | 描述 |
|------|--------|------|
| `batch.sales.report.price.threshold` | 50.0 | 销售报告的价格过滤器 |
| `batch.date.format` | yyyy-MM-dd HH:mm:ss | 导入日期格式 |

### 错误处理

| 属性 | 默认值 | 描述 |
|------|--------|------|
| `batch.retry.initial.delay` | 1000 | 初始重试延迟（毫秒） |
| `batch.retry.max.delay` | 10000 | 最大重试延迟（毫秒） |
| `batch.retry.multiplier` | 2.0 | 重试延迟倍数 |

## 🔍 监控和调试

### 应用程序日志

```bash
# 查看应用程序日志
tail -f logs/application.log

# 过滤批处理特定日志
grep "BATCH" logs/application.log
```

### 作业执行监控

应用程序为以下内容提供详细日志记录：
- 作业开始/完成状态
- 步骤执行进度
- 项目处理统计
- 错误详情和恢复操作
- 性能指标

### 健康检查

访问健康端点：
- 健康：http://localhost:8080/actuator/health
- 指标：http://localhost:8080/actuator/metrics
- 信息：http://localhost:8080/actuator/info

## 🧪 测试

### 运行所有测试

```bash
# 仅单元测试
mvn test

# 仅集成测试
mvn test -Dtest="*IntegrationTest"

# 特定测试类
mvn test -Dtest="ProductProcessorTest"
```

### 测试类别

- **单元测试**：单个组件测试
- **集成测试**：步骤和作业级别测试
- **端到端测试**：完整管道验证

## 🚨 故障排除

### 常见问题

#### 作业启动失败
```
错误：作业'productEtlJob'启动失败
解决方案：检查输入文件是否存在且可读
```

#### 数据库连接问题
```
错误：无法连接到H2数据库
解决方案：确保包含H2依赖项且配置正确
```

#### 文件未找到
```
错误：找不到输入文件
解决方案：验证batch.input.file属性中的文件路径
```

#### 内存问题
```
错误：处理过程中出现OutOfMemoryError
解决方案：减少batch.chunk.size或增加JVM堆大小
```

### 调试模式

启用调试日志记录以进行详细故障排除：

```properties
logging.level.com.example.productdataetl=DEBUG
logging.level.org.springframework.batch=DEBUG
```

## 📈 性能调优

### 块大小优化

根据您的数据测试不同的块大小：
- 小数据集（< 1K记录）：chunk.size = 10-50
- 中等数据集（1K-10K记录）：chunk.size = 100-500
- 大数据集（> 10K记录）：chunk.size = 1000+

### 内存管理

```bash
# 为大数据集增加堆大小
java -Xmx2g -jar target/product-data-etl-1.0.0.jar

# 启用GC日志记录
java -XX:+PrintGC -XX:+PrintGCDetails -jar target/product-data-etl-1.0.0.jar
```

## 🔧 自定义

### 添加新的处理步骤

1. 创建新的ItemReader、ItemProcessor和ItemWriter
2. 在BatchConfiguration中配置步骤
3. 将步骤添加到作业流程
4. 更新测试

### 自定义业务逻辑

修改处理器以实现您的特定业务规则：
- 数据验证
- 转换逻辑
- 过滤条件

### 不同的数据源

用生产数据库替换H2：
- PostgreSQL
- MySQL
- Oracle
- SQL Server

## 📚 其他资源

- [Spring Batch文档](https://spring.io/projects/spring-batch)
- [Spring Boot参考指南](https://spring.io/projects/spring-boot)
- [H2数据库文档](http://www.h2database.com/html/main.html)

## 🤝 贡献

1. Fork仓库
2. 创建功能分支
3. 为新功能添加测试
4. 确保所有测试通过
5. 提交拉取请求

## 📄 许可证

该项目根据MIT许可证授权 - 有关详细信息，请参阅LICENSE文件。