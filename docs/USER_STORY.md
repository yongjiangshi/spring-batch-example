### **User Story: Product Data ETL and Report Generation**

**As a** software developer who wants to learn Spring Batch in depth,
**I want to** build a multi-step batch job that first imports product information from a CSV file into a database, then reads this data from the database, filters and processes it, and finally generates a sales report saved as a new CSV file,
**So that** I can master how to orchestrate a complex job containing multiple steps and learn to use both file-based and database-based `ItemReader` and `ItemWriter` simultaneously.

---

### **Acceptance Criteria:**

#### **Part 1: General Setup**

1.  **Project Dependencies:**
    *   [ ] The application is a Spring Boot project that includes `spring-boot-starter-batch`, `spring-boot-starter-data-jpa`, and `h2database` dependencies.
2.  **Database:**
    *   [ ] Uses H2 in-memory database for easy testing and execution.
    *   [ ] On project startup, automatically creates a table named `PRODUCTS` through `schema.sql`, containing `id`, `name`, `description`, `price` fields.
3.  **Batch Job:**
    *   [ ] Defines a `Job` named `productEtlJob`.
    *   [ ] This `Job` contains two `Steps` executed in sequence: `step1_loadCsvToDb` and `step2_generateReportFromDb`.

---

#### **Part 2: Step 1 - CSV Import to Database**

4.  **Input Data:**
    *   [ ] The project includes a `products.csv` file with at least `id`, `name`, `description`, `price` columns and more than 10 product records.
5.  **Data Reading (Reader):**
    *   [ ] Implements a `FlatFileItemReader` to read `products.csv` and map it to `Product` JPA entity objects.
6.  **Data Processing (Processor):**
    *   [ ] (Optional, for demonstration) Implements an `ItemProcessor` that can clean or transform data (e.g., setting a default `importDate` field).
7.  **Data Writing (Writer):**
    *   [ ] Implements a `JpaItemWriter` to batch save `Product` entity objects to the H2 database's `PRODUCTS` table.
8.  **Validation:**
    *   [ ] After `Step 1` executes successfully, the `PRODUCTS` table should contain data consistent with the `products.csv` file content.

---

#### **Part 3: Step 2 - Generate Report CSV from Database**

9.  **Data Reading (Reader):**
    *   [ ] Implements a `JpaPagingItemReader` to read `Product` entities from the `PRODUCTS` table with pagination.
10. **Data Processing (Processor):**
    *   [ ] Implements an `ItemProcessor` for filtering and transforming data. For example, filter all products with `price` greater than 50 and convert them to a new `SalesReport` DTO object.
11. **Data Writing (Writer):**
    *   [ ] Implements a `FlatFileItemWriter` to write `SalesReport` DTO objects to a new file named `sales_report.csv`. The file columns can be `productId`, `productName`, `price`.
12. **Validation:**
    *   [ ] After `Step 2` executes successfully, a `sales_report.csv` file is generated in the project root directory.
    *   [ ] The data entries in `sales_report.csv` should only include products from the database where `price` is greater than 50.