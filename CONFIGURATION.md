# Configuration Guide

This document provides detailed information about all configuration options, job execution parameters, and customization possibilities for the Product Data ETL application.

## 📋 Table of Contents

- [Application Properties](#application-properties)
- [Job Execution Parameters](#job-execution-parameters)
- [Environment-Specific Configuration](#environment-specific-configuration)
- [Advanced Configuration](#advanced-configuration)
- [Performance Tuning](#performance-tuning)
- [Security Configuration](#security-configuration)

## 🔧 Application Properties

### Database Configuration

```properties
# H2 Database Settings
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA/Hibernate Settings
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
```

**Production Database Example (PostgreSQL):**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/productdb
spring.datasource.username=${DB_USERNAME:admin}
spring.datasource.password=${DB_PASSWORD:password}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Batch Processing Configuration

```properties
# Core Batch Settings
spring.batch.job.enabled=false
spring.batch.initialize-schema=always
spring.batch.job.names=productEtlJob

# Processing Parameters
batch.chunk.size=10
batch.page.size=100
batch.thread.pool.size=1
batch.thread.pool.max.size=5
batch.thread.pool.queue.capacity=25
```

### File Processing Configuration

```properties
# File Locations
batch.input.file=classpath:products.csv
batch.output.file=file:sales_report.csv

# CSV Processing Settings
batch.csv.delimiter=,
batch.csv.quote.character="
batch.csv.skip.lines=1
```

### Error Handling Configuration

```properties
# Skip and Retry Settings
batch.skip.limit=5
batch.retry.limit=3
batch.error.log.skipped.items=true

# Retry Timing
batch.retry.initial.delay=1000
batch.retry.max.delay=10000
batch.retry.multiplier=2.0
```

### Business Logic Configuration

```properties
# Business Rules
batch.sales.report.price.threshold=50.0
batch.date.format=yyyy-MM-dd HH:mm:ss
```

## 🚀 Job Execution Parameters

### Command Line Arguments

The application supports various command-line arguments for runtime configuration:

#### Basic Execution
```bash
# Standard execution
java -jar product-data-etl.jar

# With specific job name
java -jar product-data-etl.jar --spring.batch.job.names=productEtlJob
```

#### File Configuration
```bash
# Custom input file
java -jar product-data-etl.jar --batch.input.file=file:/path/to/input.csv

# Custom output file
java -jar product-data-etl.jar --batch.output.file=file:/path/to/output.csv

# Network file locations
java -jar product-data-etl.jar --batch.input.file=ftp://server/input.csv
```

#### Processing Parameters
```bash
# Custom chunk size
java -jar product-data-etl.jar --batch.chunk.size=100

# Custom page size
java -jar product-data-etl.jar --batch.page.size=500

# Custom price threshold
java -jar product-data-etl.jar --batch.sales.report.price.threshold=75.0
```

#### Error Handling Parameters
```bash
# Custom skip limit
java -jar product-data-etl.jar --batch.skip.limit=10

# Custom retry limit
java -jar product-data-etl.jar --batch.retry.limit=5

# Disable error logging
java -jar product-data-etl.jar --batch.error.log.skipped.items=false
```

### Job Parameters

Spring Batch job parameters can be passed for job identification and restart capabilities:

```bash
# Job with timestamp parameter
java -jar product-data-etl.jar --job.parameters="timestamp=$(date +%s)"

# Job with custom run ID
java -jar product-data-etl.jar --job.parameters="runId=manual-001"

# Multiple parameters
java -jar product-data-etl.jar --job.parameters="timestamp=$(date +%s),environment=production"
```

## 🌍 Environment-Specific Configuration

### Development Environment

**application-dev.properties:**
```properties
# Development settings
logging.level.com.example.productdataetl=DEBUG
spring.jpa.show-sql=true
spring.h2.console.enabled=true
batch.chunk.size=5
batch.error.log.skipped.items=true
```

### Testing Environment

**application-test.properties:**
```properties
# Test settings
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
logging.level.org.springframework.batch=WARN
batch.chunk.size=2
batch.skip.limit=1
```

### Production Environment

**application-prod.properties:**
```properties
# Production settings
logging.level.root=WARN
logging.level.com.example.productdataetl=INFO
spring.jpa.show-sql=false
spring.h2.console.enabled=false
batch.chunk.size=1000
batch.page.size=1000
batch.thread.pool.max.size=10

# Production database
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
```

### Activation
```bash
# Run with specific profile
java -jar product-data-etl.jar --spring.profiles.active=prod

# Multiple profiles
java -jar product-data-etl.jar --spring.profiles.active=prod,monitoring
```

## 🔧 Advanced Configuration

### Custom ItemReader Configuration

```properties
# File reader settings
batch.reader.file.encoding=UTF-8
batch.reader.file.strict=true
batch.reader.file.buffer.size=8192

# Database reader settings
batch.reader.db.fetch.size=100
batch.reader.db.timeout=30000
```

### Custom ItemWriter Configuration

```properties
# File writer settings
batch.writer.file.encoding=UTF-8
batch.writer.file.append=false
batch.writer.file.force.sync=true

# Database writer settings
batch.writer.db.batch.size=50
batch.writer.db.timeout=30000
```

### Thread Pool Configuration

```properties
# Async processing
batch.async.enabled=false
batch.async.core.pool.size=2
batch.async.max.pool.size=10
batch.async.queue.capacity=100
batch.async.keep.alive.seconds=60
```

### Monitoring Configuration

```properties
# JMX monitoring
spring.jmx.enabled=true
spring.jmx.default-domain=productEtl

# Actuator endpoints
management.endpoints.web.exposure.include=health,info,metrics,beans,batch
management.endpoint.health.show-details=always
management.metrics.export.simple.enabled=true
```

## ⚡ Performance Tuning

### Memory Configuration

```bash
# JVM memory settings
java -Xms512m -Xmx2g -jar product-data-etl.jar

# Garbage collection tuning
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar product-data-etl.jar

# Memory monitoring
java -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -jar product-data-etl.jar
```

### Database Performance

```properties
# Connection pool settings
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# JPA performance
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
```

### Batch Performance

```properties
# Optimal chunk sizes by data volume
# Small datasets (< 1K): 10-50
batch.chunk.size.small=25

# Medium datasets (1K-10K): 100-500
batch.chunk.size.medium=250

# Large datasets (> 10K): 1000+
batch.chunk.size.large=1000

# Page size optimization
batch.page.size=500
```

## 🔒 Security Configuration

### Database Security

```properties
# Encrypted passwords
spring.datasource.password=${DB_PASSWORD_ENCRYPTED}

# Connection encryption
spring.datasource.url=jdbc:postgresql://localhost:5432/productdb?ssl=true&sslmode=require

# Connection validation
spring.datasource.hikari.connection-test-query=SELECT 1
```

### File Security

```properties
# File permissions
batch.file.permissions=rw-r--r--

# Secure file locations
batch.input.file=file:/secure/input/products.csv
batch.output.file=file:/secure/output/sales_report.csv

# File validation
batch.file.validation.enabled=true
batch.file.validation.max.size=100MB
```

### Actuator Security

```properties
# Secure actuator endpoints
management.endpoints.web.base-path=/management
management.security.enabled=true
management.endpoints.web.exposure.include=health,info
```

## 📊 Monitoring and Metrics

### Custom Metrics

```properties
# Enable custom metrics
management.metrics.enable.batch=true
management.metrics.enable.jvm=true
management.metrics.enable.system=true

# Metric export
management.metrics.export.prometheus.enabled=true
management.metrics.export.influx.enabled=false
```

### Health Checks

```properties
# Health check configuration
management.health.batch.enabled=true
management.health.db.enabled=true
management.health.diskspace.enabled=true
management.health.diskspace.threshold=100MB
```

## 🔄 Job Restart Configuration

### Restart Parameters

```properties
# Job restart settings
spring.batch.job.restart.enabled=true
batch.job.restart.allow.start.if.complete=false

# Step restart settings
batch.step.restart.allow=true
batch.step.restart.limit=3
```

### Restart Commands

```bash
# Restart failed job
java -jar product-data-etl.jar --spring.batch.job.names=productEtlJob --restart=true

# Restart from specific step
java -jar product-data-etl.jar --restart.from.step=step2_generateReportFromDb
```

## 🧪 Testing Configuration

### Test Properties

```properties
# Test-specific settings
spring.test.database.replace=none
spring.batch.job.enabled=true
batch.test.chunk.size=2
batch.test.skip.limit=1
logging.level.org.springframework.batch.test=DEBUG
```

### Integration Test Configuration

```properties
# Integration test database
spring.datasource.url=jdbc:h2:mem:integrationtest;DB_CLOSE_DELAY=-1
spring.sql.init.mode=always
spring.batch.initialize-schema=always
```

This configuration guide provides comprehensive coverage of all available options for customizing and tuning the Product Data ETL application according to your specific requirements.