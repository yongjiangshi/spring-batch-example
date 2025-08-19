### **用户故事：产品数据ETL与报告生成**

**作为一名** 期望深入学习 Spring Batch 的软件开发者，
**我想要** 构建一个多步骤的批处理作业，该作业首先将产品信息从CSV文件导入到数据库中，然后从数据库中读取这些数据，经过筛选和处理后，最终生成一份销售报告并存为新的CSV文件，
**以便于** 我能够掌握如何编排一个包含多个步骤（Step）的复杂作业（Job），并学会同时使用基于文件和基于数据库的 `ItemReader` 与 `ItemWriter`。

---

### **验收标准 (Acceptance Criteria):**

#### **第一部分：通用设置**

1.  **项目依赖:**
    *   [ ] 应用程序是一个 Spring Boot 项目，包含 `spring-boot-starter-batch`, `spring-boot-starter-data-jpa` 和 `h2database` 依赖。
2.  **数据库:**
    *   [ ] 使用 H2 内存数据库，方便测试和运行。
    *   [ ] 项目启动时，通过 `schema.sql` 自动创建一个名为 `PRODUCTS` 的表，包含 `id`, `name`, `description`, `price` 字段。
3.  **批处理作业 (Job):**
    *   [ ] 定义一个名为 `productEtlJob` 的 `Job`。
    *   [ ] 该 `Job` 包含两个按顺序执行的 `Step`：`step1_loadCsvToDb` 和 `step2_generateReportFromDb`。

---

#### **第二部分：步骤一 (Step 1) - 从CSV导入到数据库**

4.  **输入数据:**
    *   [ ] 项目中包含一个 `products.csv` 文件，至少包含 `id`, `name`, `description`, `price` 列和10条以上的产品数据。
5.  **数据读取 (Reader):**
    *   [ ] 实现一个 `FlatFileItemReader`，用于读取 `products.csv` 并将其映射到 `Product` JPA 实体对象。
6.  **数据处理 (Processor):**
    *   [ ] (可选，用于演示) 实现一个 `ItemProcessor`，可以对数据进行清洗或转换（例如，设置一个默认的 `importDate` 字段）。
7.  **数据写入 (Writer):**
    *   [ ] 实现一个 `JpaItemWriter`，将 `Product` 实体对象批量保存到 H2 数据库的 `PRODUCTS` 表中。
8.  **验证:**
    *   [ ] `Step 1` 执行成功后，`PRODUCTS` 表中应包含与 `products.csv` 文件内容一致的数据。

---

#### **第三部分：步骤二 (Step 2) - 从数据库生成报告CSV**

9.  **数据读取 (Reader):**
    *   [ ] 实现一个 `JpaPagingItemReader`，用于从 `PRODUCTS` 表中分页读取 `Product` 实体。
10. **数据处理 (Processor):**
    *   [ ] 实现一个 `ItemProcessor`，用于筛选和转换数据。例如，筛选出所有 `price` 大于50的产品，并将其转换为一个新的 `SalesReport` DTO 对象。
11. **数据写入 (Writer):**
    *   [ ] 实现一个 `FlatFileItemWriter`，将 `SalesReport` DTO 对象写入到一个名为 `sales_report.csv` 的新文件中。该文件的列可以是 `productId`, `productName`, `price`。
12. **验证:**
    *   [ ] `Step 2` 执行成功后，项目根目录下会生成 `sales_report.csv` 文件。
    *   [ ] `sales_report.csv` 文件中的数据条目应只包含那些在数据库中 `price` 大于50的产品。
