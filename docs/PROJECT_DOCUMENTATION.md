# Spring Batch ETL Project - Technical Documentation

## 1. Architecture and Design Overview

### 1.1. Project Goal and Scope
This project aims to implement a robust and scalable ETL (Extract, Transform, Load) batch processing pipeline. Its core functionality is to extract product data from a CSV file, load it into a relational database after validation and transformation, and subsequently extract and process data from the database to generate a sales report in CSV format. This project is not just a functional implementation but also a showcase of best practices for the Spring Batch framework.

### 1.2. Technology Stack
- **Core Frameworks:** Spring Boot 3.2, Spring Batch 5.1
- **Data Persistence:** Spring Data JPA, Hibernate
- **Database:** H2 In-Memory Database
- **Build Tool:** Apache Maven
- **Language:** Java 17

### 1.3. Architectural Design
The project adopts a classic **Multi-Step Job** architecture, breaking down the entire ETL process into two independent, sequential **Steps**. This design adheres to the **Single Responsibility Principle**, making the logic of each step cohesive, easy to understand, test, and maintain.

- **Step 1 (`step1_loadCsvToDb`):** Responsible for data loading from the file system to the database.
- **Step 2 (`step2_generateReportFromDb`):** Responsible for data extraction and report generation from the database to the file system.

The entire job flow is triggered by a `JobLauncher` and persists its execution state via a `JobRepository`, thereby achieving job **restartability** and **idempotence**.

![Architecture Diagram](https://i.imgur.com/rY8kL0g.png) 
*(This is a schematic diagram describing the data flow)*

---

## 2. In-depth Analysis of Core Spring Batch Components

### 2.1. Job State Persistence: `JobRepository`
The `JobRepository` is the cornerstone of Spring Batch, responsible for persisting the execution metadata of `Job`s and `Step`s (such as status, start/end times, read/write counts, etc.) to a database. This metadata is typically stored in a series of tables prefixed with `BATCH_` (e.g., `BATCH_JOB_INSTANCE`, `BATCH_STEP_EXECUTION`).

**Key Roles:**
- **Restartability:** If a job fails unexpectedly, the state recorded in the `JobRepository` allows Spring Batch to resume from the failed `Step` on the next run, rather than starting from the beginning, thus avoiding re-execution of successful operations.
- **Concurrency Control:** Ensures that the same `JobInstance` (defined by the job name and identifying parameters) does not run concurrently.

In `schema.sql`, we create indexes for the `PRODUCTS` table. Similarly, creating indexes for the `BATCH_` series of tables is crucial for improving the performance of batch jobs under high concurrency.

### 2.2. Chunk-Oriented Processing and Transaction Management
All `Step`s in this project use the **Chunk-Oriented Processing** model.

```java
// In BatchConfiguration.java
.chunk(10, transactionManager)
```

This configuration defines a transaction boundary. Its workflow is as follows:
1.  **Begin Transaction**.
2.  The `ItemReader` is called 10 times to read 10 `item`s.
3.  These 10 `item`s are sent in batches to the `ItemProcessor` for processing.
4.  The list of processed `item`s (which may be fewer than 10 if the `Processor` filters some out) is passed to the `ItemWriter`.
5.  The `ItemWriter` writes the entire list of data to the target.
6.  **Commit Transaction**.

If an exception is thrown at any stage of this process (especially in the `Writer`), the entire transaction will be **rolled back**, and all changes for these 10 items will be nullified, ensuring data integrity.

---

## 3. Process Analysis: Step 1 - CSV to Database

### 3.1. `FlatFileItemReader`: File Reading
This `Reader` is responsible for parsing `products.csv`. Internally, it uses a `DefaultLineMapper` to map each line of text from the file to a `Product` object. The `LineMapper` typically consists of two parts:
- `LineTokenizer`: Splits a line of text into fields based on a delimiter (a comma in this project).
- `FieldSetMapper`: Maps the resulting set of fields (`FieldSet`) to the properties of a `Product` object.

### 3.2. `ProductProcessor`: Data Validation and Transformation
This `Processor` is the final gatekeeper before data enters the database, undertaking multiple responsibilities:
- **Data Validation:** Checks the validity of key fields like `id`, `name`, and `price`. Invalid data will be filtered out (by returning `null`), preventing "dirty data" from polluting the database.
- **Data Cleansing:** Performs a `trim()` operation on string fields to ensure data is well-formed.
- **Data Enrichment:** Adds an `importDate` timestamp to each record to log its import time.

### 3.3. `JpaItemWriter`: Database Writing
`JpaItemWriter` utilizes the JPA `EntityManager` to perform database writes. It does not simply execute an `INSERT` for each `item`. Instead, it `merge`s all `Product` entities in a `chunk` into the current persistence context. When the transaction commits, the **batching mechanism of Hibernate/JPA (JDBC Batching)** is triggered, combining multiple `INSERT` statements into a single network call to the database, thereby significantly improving write performance.

---

## 4. Process Analysis: Step 2 - Database to CSV

### 4.1. `JpaPagingItemReader`: Scalable Database Reading
When reading large amounts of data from a database, loading all data into memory at once is extremely risky. `JpaPagingItemReader` solves this problem through paged queries.
- **How it works:** It fetches data in batches by executing SQL queries with `LIMIT` and `OFFSET` (or the database equivalent). Each time a `read()` call goes beyond the current page, it automatically fetches the next page of data.
- **Comparison with `CursorItemReader`:** The `Cursor` approach holds a database connection and cursor for an extended period, which can lead to resource exhaustion or connection timeouts in distributed or high-concurrency environments. The `Paging` approach, however, is stateless, with each query being independent, making it more robust and scalable.

### 4.2. `SalesReportProcessor`: Business Filtering and Data Transformation
This `Processor` once again demonstrates its core roles:
- **Business Filtering:** Filters data based on business rules (`price > 50`). Returning `null` is the standard pattern for implementing filtering in an `ItemProcessor`.
- **Data Transformation:** Converts a persistence layer `Product` entity into a `SalesReport` **DTO (Data Transfer Object)**, which is specialized for the report.

**Why use a DTO?**
This is an important design pattern. It decouples the **data persistence model** from the **external interface/report model**. If the report needs to add or remove fields or change formatting in the future, we only need to modify the `SalesReport` DTO and the `Processor`, without touching the core `Product` entity and database structure.

### 4.3. `FlatFileItemWriter`: Report Generation
This `Writer` is responsible for converting `SalesReport` DTO objects into CSV-formatted strings and writing them to a file. It is typically configured with a `BeanWrapperFieldExtractor` to extract fields from the DTO, and a `DelimitedLineAggregator` to join them with commas into a single line of text.

---

## 5. Advanced Feature Analysis

### 5.1. Fault Tolerance: Skip & Retry
The `.faultTolerant()` in `BatchConfiguration` enables advanced fault tolerance features.
- **`CustomSkipPolicy`:** Provides fine-grained control over the "skip" logic. It categorizes exceptions into two types:
    - **Skippable Exceptions:** Such as `FlatFileParseException`. These exceptions are typically caused by a single "dirty" record; the job should skip this record and continue.
    - **Fatal Exceptions:** Such as `TransientDatabaseException`. These exceptions indicate system-level problems and should not be skipped but should trigger a retry or cause the job to fail.
- **`CustomRetryPolicy`:** Provides retry capabilities for **transient failures** (like database deadlocks or network jitters). It integrates with the `spring-retry` library to re-execute the failed operation after a delay (backoff-policy). If successful within a specified number of attempts, the job continues; otherwise, it fails.

### 5.2. Job Monitoring: Listeners
`Listener`s are the "Aspects (AOP)" of Spring Batch, allowing us to inject custom logic at key points in the `Job` and `Step` lifecycle.
- **`DetailedJobExecutionListener`:** Provides macroscopic job monitoring. After a job completes, it aggregates statistics from all steps, calculates total duration and success rates, and can even provide optimization suggestions based on the results (e.g., high skip rate, long duration).
- **`DetailedStepExecutionListener`:** Provides microscopic performance insights. After each `Step` completes, it calculates the processing rate **(items/second)** and memory usage for that step, providing key data for performance bottleneck analysis.

The logs produced by these listeners transform a black-box batch process into a fully observable and transparent system.

## 6. Conclusion and Best Practices

This project's code demonstrates several best practices for building production-grade Spring Batch applications:
1.  **Separation of Concerns:** Clearly separating the read, process, and write logic into different components.
2.  **Programming to Interfaces:** `ItemReader`, `ItemProcessor`, and `ItemWriter` are all interfaces, making them easy to replace and test.
3.  **Scalable Design:** Using `JpaPagingItemReader` ensures performance and stability when processing large datasets.
4.  **Robust Error Handling:** Gracefully handling data-level and system-level exceptions through `Skip` and `Retry` strategies.
5.  **High Observability:** Implementing deep monitoring and logging through custom `Listener`s.
6.  **Externalized Configuration:** Placing batch parameters (like `chunkSize`) in `application.properties` for easy tuning in different environments.
7.  **Decoupling with DTOs:** Isolating the internal data model from the external data representation.
