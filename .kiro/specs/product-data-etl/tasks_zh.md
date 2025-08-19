# 实施计划

- [x] 1. 建立Spring Boot项目结构和依赖项
  - 创建带有Spring Boot starter的Maven/Gradle项目
  - 添加spring-boot-starter-batch、spring-boot-starter-data-jpa和h2database依赖项
  - 为H2数据库和批处理设置配置application.properties
  - _需求: 1.1, 1.2_

- [x] 2. 创建数据库模式和初始化
  - 编写schema.sql以创建包含id、name、description、price、import_date列的PRODUCTS表
  - 配置Spring Boot在启动时自动执行schema.sql
  - _需求: 1.3_

- [x] 3. 实现数据模型和实体
  - 创建带有适当注解和字段映射的Product JPA实体类
  - 创建用于报告生成的SalesReport DTO类
  - 为实体验证和映射编写单元测试
  - _需求: 3.2, 4.3_

- [x] 4. 创建示例CSV输入数据
  - 创建包含至少10条产品记录的products.csv文件，包括不同的价格范围
  - 将文件放置在src/main/resources目录中以便类路径访问
  - 包括价格高于和低于50的产品以进行过滤演示
  - _需求: 3.1_

- [x] 5. 实现步骤1组件（CSV到数据库）
- [x] 5.1 创建ProductCsvReader
  - 实现FlatFileItemReader<Product>配置
  - 配置DelimitedLineTokenizer和BeanWrapperFieldSetMapper用于CSV解析
  - 为CSV读取和Product对象映射编写单元测试
  - _需求: 3.2_

- [x] 5.2 创建ProductProcessor
  - 实现ItemProcessor<Product, Product>用于数据转换
  - 添加将importDate字段设置为当前时间戳的逻辑
  - 包括数据验证和清理逻辑
  - 为处理器转换逻辑编写单元测试
  - _需求: 3.3_

- [x] 5.3 创建ProductWriter
  - 实现JpaItemWriter<Product>配置
  - 配置EntityManagerFactory注入用于数据库操作
  - 为数据库持久化操作编写单元测试
  - _需求: 3.4_

- [x] 6. 实现步骤2组件（数据库到报告）
- [x] 6.1 创建用于数据库访问的ProductReader
  - 实现JpaPagingItemReader<Product>配置
  - 配置JPA查询以适当排序读取所有产品
  - 设置适当的页面大小以实现高效的内存使用
  - 为分页数据库读取编写单元测试
  - _需求: 4.1_

- [x] 6.2 创建SalesReportProcessor
  - 实现ItemProcessor<Product, SalesReport>用于过滤和转换
  - 添加过滤价格大于50的产品的逻辑
  - 将Product实体转换为SalesReport DTO对象
  - 为过滤逻辑和DTO转换编写单元测试
  - _需求: 4.2, 4.3_

- [x] 6.3 创建SalesReportWriter
  - 实现FlatFileItemWriter<SalesReport>配置
  - 配置DelimitedLineAggregator用于CSV输出格式化
  - 设置sales_report.csv的输出文件位置和标题
  - 为CSV文件生成编写单元测试
  - _需求: 4.4_

- [x] 7. 配置Spring Batch作业和步骤
- [x] 7.1 创建批处理配置类
  - 定义JobBuilderFactory和StepBuilderFactory bean
  - 配置面向块的处理，使用适当的块大小
  - 设置适当的事务管理和错误处理
  - _需求: 2.1, 2.2_

- [x] 7.2 定义步骤1配置
  - 创建带有reader、processor和writer的step1_loadCsvToDb步骤bean
  - 配置块大小、跳过策略和重试逻辑
  - 添加步骤执行监听器用于日志记录和监控
  - _需求: 2.2, 3.5_

- [x] 7.3 定义步骤2配置
  - 创建带有reader、processor和writer的step2_generateReportFromDb步骤bean
  - 配置块大小和错误处理策略
  - 添加步骤执行监听器用于进度跟踪
  - _需求: 2.2, 4.5_

- [x] 7.4 创建productEtlJob配置
  - 定义执行step1然后step2的Job bean
  - 配置具有适当步骤排序的作业流程
  - 添加作业执行监听器用于整体作业监控
  - _需求: 2.1, 2.3_

- [x] 8. 实现错误处理和日志记录
  - 配置跳过策略以处理无效的CSV记录
  - 实现瞬态数据库错误的重试逻辑
  - 在步骤和作业级别添加全面的日志记录
  - 为错误场景和恢复机制编写测试
  - _需求: 5.1, 5.2, 5.3, 5.4_

- [x] 9. 创建应用程序运行器和主类
  - 实现CommandLineRunner以在应用程序启动时触发作业执行
  - 配置JobLauncher以使用适当参数运行productEtlJob
  - 添加不同执行模式的命令行参数处理
  - _需求: 2.1_

- [x] 10. 编写集成测试
- [x] 10.1 创建步骤级集成测试
  - 使用示例CSV数据测试步骤1执行并验证数据库状态
  - 使用数据库数据测试步骤2执行并验证输出CSV文件
  - 验证数据完整性和转换准确性
  - _需求: 3.5, 4.5, 4.6_

- [x] 10.2 创建端到端作业集成测试
  - 测试从CSV输入到报告输出的完整作业执行
  - 验证作业完成状态和步骤执行顺序
  - 测试作业重启和失败恢复场景
  - _需求: 2.4, 5.1, 5.2_

- [x] 11. 添加配置和文档
  - 创建包含所有批处理配置的全面application.properties
  - 添加包含设置说明和使用示例的README.md
  - 记录作业执行参数和配置选项
  - _需求: 1.1, 1.2_