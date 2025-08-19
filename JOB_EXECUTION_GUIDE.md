# Job Execution Quick Reference Guide

This guide provides quick reference for executing the Product Data ETL job with various parameters and configurations.

## 🚀 Quick Start Commands

### Basic Execution
```bash
# Run with default settings
mvn spring-boot:run

# Run packaged JAR
java -jar target/product-data-etl-0.0.1-SNAPSHOT.jar
```

### Common Execution Scenarios

#### 1. Custom Input/Output Files
```bash
# Custom CSV input file
java -jar product-data-etl.jar \
  --batch.input.file=file:/path/to/custom-products.csv

# Custom output location
java -jar product-data-etl.jar \
  --batch.output.file=file:/path/to/custom-report.csv

# Both custom input and output
java -jar product-data-etl.jar \
  --batch.input.file=file:/data/products.csv \
  --batch.output.file=file:/reports/sales_report.csv
```

#### 2. Performance Tuning
```bash
# Large dataset processing
java -Xmx4g -jar product-data-etl.jar \
  --batch.chunk.size=1000 \
  --batch.page.size=1000

# Small dataset with detailed logging
java -jar product-data-etl.jar \
  --batch.chunk.size=5 \
  --logging.level.com.example.productdataetl=DEBUG
```

#### 3. Error Handling Customization
```bash
# Strict error handling (fail fast)
java -jar product-data-etl.jar \
  --batch.skip.limit=0 \
  --batch.retry.limit=1

# Lenient error handling
java -jar product-data-etl.jar \
  --batch.skip.limit=100 \
  --batch.retry.limit=5 \
  --batch.error.log.skipped.items=true
```

#### 4. Business Logic Customization
```bash
# Different price threshold for sales report
java -jar product-data-etl.jar \
  --batch.sales.report.price.threshold=75.0

# Custom date format
java -jar product-data-etl.jar \
  --batch.date.format="dd/MM/yyyy HH:mm:ss"
```

## 📋 Parameter Reference

### File Processing Parameters

| Parameter | Default | Description | Example |
|-----------|---------|-------------|---------|
| `batch.input.file` | classpath:products.csv | Input CSV file location | `file:/data/products.csv` |
| `batch.output.file` | file:sales_report.csv | Output report file location | `file:/reports/sales.csv` |
| `batch.csv.delimiter` | , | CSV field delimiter | `;` or `\|` |
| `batch.csv.skip.lines` | 1 | Number of header lines to skip | `0` or `2` |

### Processing Parameters

| Parameter | Default | Description | Example |
|-----------|---------|-------------|---------|
| `batch.chunk.size` | 10 | Items per transaction chunk | `100` or `1000` |
| `batch.page.size` | 100 | Database pagination size | `500` or `1000` |
| `batch.thread.pool.size` | 1 | Core thread pool size | `2` or `4` |

### Error Handling Parameters

| Parameter | Default | Description | Example |
|-----------|---------|-------------|---------|
| `batch.skip.limit` | 5 | Max items to skip | `0` (fail fast) or `100` |
| `batch.retry.limit` | 3 | Max retry attempts | `1` or `10` |
| `batch.retry.initial.delay` | 1000 | Initial retry delay (ms) | `500` or `2000` |
| `batch.error.log.skipped.items` | true | Log skipped items | `false` |

### Business Logic Parameters

| Parameter | Default | Description | Example |
|-----------|---------|-------------|---------|
| `batch.sales.report.price.threshold` | 50.0 | Price filter threshold | `25.0` or `100.0` |
| `batch.date.format` | yyyy-MM-dd HH:mm:ss | Import date format | `dd/MM/yyyy` |

## 🔧 Environment-Specific Execution

### Development Environment
```bash
java -jar product-data-etl.jar \
  --spring.profiles.active=dev \
  --logging.level.com.example.productdataetl=DEBUG \
  --batch.chunk.size=5
```

### Testing Environment
```bash
java -jar product-data-etl.jar \
  --spring.profiles.active=test \
  --batch.input.file=classpath:test-products.csv \
  --batch.chunk.size=2
```

### Production Environment
```bash
java -Xmx2g -jar product-data-etl.jar \
  --spring.profiles.active=prod \
  --batch.chunk.size=1000 \
  --batch.page.size=1000 \
  --logging.level.root=WARN
```

## 🔄 Job Restart and Recovery

### Restart Failed Job
```bash
# Restart with same parameters
java -jar product-data-etl.jar \
  --spring.batch.job.names=productEtlJob \
  --restart=true

# Restart with different parameters
java -jar product-data-etl.jar \
  --spring.batch.job.names=productEtlJob \
  --restart=true \
  --batch.chunk.size=50
```

### Job Parameters for Unique Execution
```bash
# Add timestamp for unique job instance
java -jar product-data-etl.jar \
  --job.parameters="timestamp=$(date +%s)"

# Add custom run identifier
java -jar product-data-etl.jar \
  --job.parameters="runId=manual-$(date +%Y%m%d-%H%M%S)"
```

## 📊 Monitoring and Debugging

### Debug Mode
```bash
# Enable debug logging
java -jar product-data-etl.jar \
  --logging.level.com.example.productdataetl=DEBUG \
  --logging.level.org.springframework.batch=DEBUG

# SQL query logging
java -jar product-data-etl.jar \
  --spring.jpa.show-sql=true \
  --logging.level.org.hibernate.SQL=DEBUG
```

### Performance Monitoring
```bash
# Enable JMX monitoring
java -Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=9999 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  -jar product-data-etl.jar

# Enable actuator endpoints
java -jar product-data-etl.jar \
  --management.endpoints.web.exposure.include=health,metrics,batch
```

## 🚨 Troubleshooting Commands

### File Issues
```bash
# Check file permissions
ls -la /path/to/input/products.csv

# Test with absolute path
java -jar product-data-etl.jar \
  --batch.input.file=file:/absolute/path/to/products.csv
```

### Memory Issues
```bash
# Increase heap size
java -Xmx4g -jar product-data-etl.jar

# Enable GC logging
java -XX:+PrintGC -XX:+PrintGCDetails \
  -Xloggc:gc.log \
  -jar product-data-etl.jar
```

### Database Issues
```bash
# Enable H2 console for debugging
java -jar product-data-etl.jar \
  --spring.h2.console.enabled=true

# Test with external database
java -jar product-data-etl.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/testdb \
  --spring.datasource.username=testuser \
  --spring.datasource.password=testpass
```

## 📝 Example Execution Scripts

### Linux/Mac Script (run.sh)
```bash
#!/bin/bash

# Set variables
JAR_FILE="target/product-data-etl-0.0.1-SNAPSHOT.jar"
INPUT_FILE="/data/products.csv"
OUTPUT_FILE="/reports/sales_report_$(date +%Y%m%d).csv"

# Execute job
java -Xmx2g -jar "$JAR_FILE" \
  --batch.input.file="file:$INPUT_FILE" \
  --batch.output.file="file:$OUTPUT_FILE" \
  --batch.chunk.size=500 \
  --job.parameters="timestamp=$(date +%s)"

echo "Job completed. Report saved to: $OUTPUT_FILE"
```

### Windows Script (run.bat)
```batch
@echo off

set JAR_FILE=target\product-data-etl-0.0.1-SNAPSHOT.jar
set INPUT_FILE=C:\data\products.csv
set OUTPUT_FILE=C:\reports\sales_report_%date:~-4,4%%date:~-10,2%%date:~-7,2%.csv

java -Xmx2g -jar "%JAR_FILE%" ^
  --batch.input.file="file:%INPUT_FILE%" ^
  --batch.output.file="file:%OUTPUT_FILE%" ^
  --batch.chunk.size=500

echo Job completed. Report saved to: %OUTPUT_FILE%
```

This quick reference guide provides all the essential commands and parameters needed to execute the Product Data ETL job effectively in various scenarios.