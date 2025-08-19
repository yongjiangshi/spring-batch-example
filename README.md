# Product Data ETL - Spring Batch Application

A comprehensive Spring Batch ETL (Extract, Transform, Load) pipeline that demonstrates advanced batch processing capabilities. This application processes product data through a multi-step workflow: importing CSV data into a database, then generating filtered sales reports.

## 🚀 Features

- **Multi-step Batch Processing**: Two-step ETL pipeline with proper sequencing
- **File-to-Database Processing**: CSV import with data validation and transformation
- **Database-to-File Processing**: Filtered report generation with custom business logic
- **Robust Error Handling**: Skip policies, retry mechanisms, and comprehensive logging
- **Spring Boot Integration**: Modern Spring Boot application with auto-configuration
- **In-Memory Database**: H2 database for easy development and testing
- **Comprehensive Testing**: Unit and integration tests with high coverage

## 📋 Prerequisites

- Java 11 or higher
- Maven 3.6+ or Gradle 6+
- IDE (IntelliJ IDEA, Eclipse, or VS Code recommended)

## 🛠️ Setup Instructions

### 1. Clone and Build

```bash
# Clone the repository
git clone <repository-url>
cd product-data-etl

# Build the project
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package
```

### 2. Database Setup

The application uses H2 in-memory database that is automatically configured. No additional setup required.

**H2 Console Access:**
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

### 3. Input Data Preparation

Place your CSV file at `src/main/resources/products.csv` with the following format:

```csv
id,name,description,price
1,Laptop,High-performance laptop,999.99
2,Mouse,Wireless mouse,25.50
3,Keyboard,Mechanical keyboard,75.00
```

## 🏃‍♂️ Running the Application

### Standard Execution

```bash
# Run the complete ETL pipeline
mvn spring-boot:run

# Or run the JAR file
java -jar target/product-data-etl-1.0.0.jar
```

### Custom Configuration

```bash
# Run with custom input file
java -jar target/product-data-etl-1.0.0.jar --batch.input.file=file:/path/to/custom.csv

# Run with custom output location
java -jar target/product-data-etl-1.0.0.jar --batch.output.file=file:/path/to/custom_report.csv

# Run with custom chunk size
java -jar target/product-data-etl-1.0.0.jar --batch.chunk.size=50
```

### Development Mode

```bash
# Run with debug logging
java -jar target/product-data-etl-1.0.0.jar --logging.level.com.example.productdataetl=DEBUG

# Run with SQL logging enabled
java -jar target/product-data-etl-1.0.0.jar --spring.jpa.show-sql=true
```

## 📊 Job Execution Flow

### Step 1: CSV to Database Import
1. **Read**: Products from CSV file using `FlatFileItemReader`
2. **Process**: Add import timestamp and validate data
3. **Write**: Persist to PRODUCTS table using `JpaItemWriter`

### Step 2: Database to Report Generation
1. **Read**: Products from database using `JpaPagingItemReader`
2. **Process**: Filter products with price > $50 and transform to report format
3. **Write**: Generate sales report CSV using `FlatFileItemWriter`

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/example/productdataetl/
│   │   ├── config/           # Batch configuration classes
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── exception/        # Custom exceptions
│   │   ├── listener/         # Job and step listeners
│   │   ├── model/            # JPA entities
│   │   ├── processor/        # Item processors
│   │   ├── reader/           # Item readers
│   │   └── writer/           # Item writers
│   └── resources/
│       ├── application.properties  # Configuration
│       ├── schema.sql             # Database schema
│       └── products.csv           # Sample input data
└── test/
    ├── java/                 # Unit and integration tests
    └── resources/
        └── test-products.csv # Test data
```

## ⚙️ Configuration Options

### Core Batch Settings

| Property | Default | Description |
|----------|---------|-------------|
| `batch.chunk.size` | 10 | Items processed per transaction |
| `batch.page.size` | 100 | Database pagination size |
| `batch.skip.limit` | 5 | Max items to skip before job failure |
| `batch.retry.limit` | 3 | Max retry attempts for errors |

### File Processing

| Property | Default | Description |
|----------|---------|-------------|
| `batch.input.file` | classpath:products.csv | Input CSV file location |
| `batch.output.file` | file:sales_report.csv | Output report file location |
| `batch.csv.delimiter` | , | CSV field delimiter |
| `batch.csv.skip.lines` | 1 | Header lines to skip |

### Business Logic

| Property | Default | Description |
|----------|---------|-------------|
| `batch.sales.report.price.threshold` | 50.0 | Price filter for sales report |
| `batch.date.format` | yyyy-MM-dd HH:mm:ss | Import date format |

### Error Handling

| Property | Default | Description |
|----------|---------|-------------|
| `batch.retry.initial.delay` | 1000 | Initial retry delay (ms) |
| `batch.retry.max.delay` | 10000 | Maximum retry delay (ms) |
| `batch.retry.multiplier` | 2.0 | Retry delay multiplier |

## 🔍 Monitoring and Debugging

### Application Logs

```bash
# View application logs
tail -f logs/application.log

# Filter batch-specific logs
grep "BATCH" logs/application.log
```

### Job Execution Monitoring

The application provides detailed logging for:
- Job start/completion status
- Step execution progress
- Item processing statistics
- Error details and recovery actions
- Performance metrics

### Health Checks

Access health endpoints:
- Health: http://localhost:8080/actuator/health
- Metrics: http://localhost:8080/actuator/metrics
- Info: http://localhost:8080/actuator/info

## 🧪 Testing

### Run All Tests

```bash
# Unit tests only
mvn test

# Integration tests only
mvn test -Dtest="*IntegrationTest"

# Specific test class
mvn test -Dtest="ProductProcessorTest"
```

### Test Categories

- **Unit Tests**: Individual component testing
- **Integration Tests**: Step and job-level testing
- **End-to-End Tests**: Complete pipeline validation

## 🚨 Troubleshooting

### Common Issues

#### Job Fails to Start
```
Error: Job 'productEtlJob' failed to start
Solution: Check input file exists and is readable
```

#### Database Connection Issues
```
Error: Cannot connect to H2 database
Solution: Ensure H2 dependency is included and configuration is correct
```

#### File Not Found
```
Error: Input file not found
Solution: Verify file path in batch.input.file property
```

#### Memory Issues
```
Error: OutOfMemoryError during processing
Solution: Reduce batch.chunk.size or increase JVM heap size
```

### Debug Mode

Enable debug logging for detailed troubleshooting:

```properties
logging.level.com.example.productdataetl=DEBUG
logging.level.org.springframework.batch=DEBUG
```

## 📈 Performance Tuning

### Chunk Size Optimization

Test different chunk sizes based on your data:
- Small datasets (< 1K records): chunk.size = 10-50
- Medium datasets (1K-10K records): chunk.size = 100-500
- Large datasets (> 10K records): chunk.size = 1000+

### Memory Management

```bash
# Increase heap size for large datasets
java -Xmx2g -jar target/product-data-etl-1.0.0.jar

# Enable GC logging
java -XX:+PrintGC -XX:+PrintGCDetails -jar target/product-data-etl-1.0.0.jar
```

## 🔧 Customization

### Adding New Processing Steps

1. Create new ItemReader, ItemProcessor, and ItemWriter
2. Configure step in BatchConfiguration
3. Add step to job flow
4. Update tests

### Custom Business Logic

Modify processors to implement your specific business rules:
- Data validation
- Transformation logic
- Filtering criteria

### Different Data Sources

Replace H2 with production databases:
- PostgreSQL
- MySQL
- Oracle
- SQL Server

## 📚 Additional Resources

- [Spring Batch Documentation](https://spring.io/projects/spring-batch)
- [Spring Boot Reference Guide](https://spring.io/projects/spring-boot)
- [H2 Database Documentation](http://www.h2database.com/html/main.html)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.