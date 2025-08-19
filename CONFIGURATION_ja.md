# 設定ガイド

このドキュメントは、製品データETLアプリケーションのすべての設定オプション、ジョブ実行パラメータ、およびカスタマイゼーションの可能性に関する詳細情報を提供します。

## 📋 目次

- [アプリケーションプロパティ](#アプリケーションプロパティ)
- [ジョブ実行パラメータ](#ジョブ実行パラメータ)
- [環境固有の設定](#環境固有の設定)
- [高度な設定](#高度な設定)
- [パフォーマンスチューニング](#パフォーマンスチューニング)
- [セキュリティ設定](#セキュリティ設定)

## 🔧 アプリケーションプロパティ

### データベース設定

```properties
# H2データベース設定
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA/Hibernate設定
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
```

**本番データベースの例（PostgreSQL）：**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/productdb
spring.datasource.username=${DB_USERNAME:admin}
spring.datasource.password=${DB_PASSWORD:password}
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### バッチ処理設定

```properties
# コアバッチ設定
spring.batch.job.enabled=false
spring.batch.initialize-schema=always
spring.batch.job.names=productEtlJob

# 処理パラメータ
batch.chunk.size=10
batch.page.size=100
batch.thread.pool.size=1
batch.thread.pool.max.size=5
batch.thread.pool.queue.capacity=25
```

### ファイル処理設定

```properties
# ファイルの場所
batch.input.file=classpath:products.csv
batch.output.file=file:sales_report.csv

# CSV処理設定
batch.csv.delimiter=,
batch.csv.quote.character="
batch.csv.skip.lines=1
```

### エラーハンドリング設定

```properties
# スキップとリトライ設定
batch.skip.limit=5
batch.retry.limit=3
batch.error.log.skipped.items=true

# リトライタイミング
batch.retry.initial.delay=1000
batch.retry.max.delay=10000
batch.retry.multiplier=2.0
```

### ビジネスロジック設定

```properties
# ビジネスルール
batch.sales.report.price.threshold=50.0
batch.date.format=yyyy-MM-dd HH:mm:ss
```

## 🚀 ジョブ実行パラメータ

### コマンドライン引数

アプリケーションは実行時設定のための様々なコマンドライン引数をサポートします：

#### 基本実行
```bash
# 標準実行
java -jar product-data-etl.jar

# 特定のジョブ名で実行
java -jar product-data-etl.jar --spring.batch.job.names=productEtlJob
```

#### ファイル設定
```bash
# カスタム入力ファイル
java -jar product-data-etl.jar --batch.input.file=file:/path/to/input.csv

# カスタム出力ファイル
java -jar product-data-etl.jar --batch.output.file=file:/path/to/output.csv

# ネットワークファイルの場所
java -jar product-data-etl.jar --batch.input.file=ftp://server/input.csv
```

#### 処理パラメータ
```bash
# カスタムチャンクサイズ
java -jar product-data-etl.jar --batch.chunk.size=100

# カスタムページサイズ
java -jar product-data-etl.jar --batch.page.size=500

# カスタム価格閾値
java -jar product-data-etl.jar --batch.sales.report.price.threshold=75.0
```

#### エラーハンドリングパラメータ
```bash
# カスタムスキップ制限
java -jar product-data-etl.jar --batch.skip.limit=10

# カスタムリトライ制限
java -jar product-data-etl.jar --batch.retry.limit=5

# エラーログを無効化
java -jar product-data-etl.jar --batch.error.log.skipped.items=false
```

### ジョブパラメータ

Spring Batchジョブパラメータは、ジョブ識別と再起動機能のために渡すことができます：

```bash
# タイムスタンプパラメータ付きジョブ
java -jar product-data-etl.jar --job.parameters="timestamp=$(date +%s)"

# カスタム実行IDを持つジョブ
java -jar product-data-etl.jar --job.parameters="runId=manual-001"

# 複数パラメータ
java -jar product-data-etl.jar --job.parameters="timestamp=$(date +%s),environment=production"
```

## 🌍 環境固有の設定

### 開発環境

**application-dev.properties：**
```properties
# 開発設定
logging.level.com.example.productdataetl=DEBUG
spring.jpa.show-sql=true
spring.h2.console.enabled=true
batch.chunk.size=5
batch.error.log.skipped.items=true
```

### テスト環境

**application-test.properties：**
```properties
# テスト設定
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
logging.level.org.springframework.batch=WARN
batch.chunk.size=2
batch.skip.limit=1
```

### 本番環境

**application-prod.properties：**
```properties
# 本番設定
logging.level.root=WARN
logging.level.com.example.productdataetl=INFO
spring.jpa.show-sql=false
spring.h2.console.enabled=false
batch.chunk.size=1000
batch.page.size=1000
batch.thread.pool.max.size=10

# 本番データベース
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
```

### アクティベーション
```bash
# 特定のプロファイルで実行
java -jar product-data-etl.jar --spring.profiles.active=prod

# 複数プロファイル
java -jar product-data-etl.jar --spring.profiles.active=prod,monitoring
```

## 🔧 高度な設定

### カスタムItemReader設定

```properties
# ファイルリーダー設定
batch.reader.file.encoding=UTF-8
batch.reader.file.strict=true
batch.reader.file.buffer.size=8192

# データベースリーダー設定
batch.reader.db.fetch.size=100
batch.reader.db.timeout=30000
```

### カスタムItemWriter設定

```properties
# ファイルライター設定
batch.writer.file.encoding=UTF-8
batch.writer.file.append=false
batch.writer.file.force.sync=true

# データベースライター設定
batch.writer.db.batch.size=50
batch.writer.db.timeout=30000
```

### スレッドプール設定

```properties
# 非同期処理
batch.async.enabled=false
batch.async.core.pool.size=2
batch.async.max.pool.size=10
batch.async.queue.capacity=100
batch.async.keep.alive.seconds=60
```

### 監視設定

```properties
# JMX監視
spring.jmx.enabled=true
spring.jmx.default-domain=productEtl

# Actuatorエンドポイント
management.endpoints.web.exposure.include=health,info,metrics,beans,batch
management.endpoint.health.show-details=always
management.metrics.export.simple.enabled=true
```

## ⚡ パフォーマンスチューニング

### メモリ設定

```bash
# JVMメモリ設定
java -Xms512m -Xmx2g -jar product-data-etl.jar

# ガベージコレクションチューニング
java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar product-data-etl.jar

# メモリ監視
java -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -jar product-data-etl.jar
```

### データベースパフォーマンス

```properties
# コネクションプール設定
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# JPAパフォーマンス
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
```

### バッチパフォーマンス

```properties
# データ量による最適なチャンクサイズ
# 小さなデータセット（< 1K）：10-50
batch.chunk.size.small=25

# 中程度のデータセット（1K-10K）：100-500
batch.chunk.size.medium=250

# 大きなデータセット（> 10K）：1000+
batch.chunk.size.large=1000

# ページサイズ最適化
batch.page.size=500
```

## 🔒 セキュリティ設定

### データベースセキュリティ

```properties
# 暗号化されたパスワード
spring.datasource.password=${DB_PASSWORD_ENCRYPTED}

# 接続暗号化
spring.datasource.url=jdbc:postgresql://localhost:5432/productdb?ssl=true&sslmode=require

# 接続検証
spring.datasource.hikari.connection-test-query=SELECT 1
```

### ファイルセキュリティ

```properties
# ファイル権限
batch.file.permissions=rw-r--r--

# セキュアなファイルの場所
batch.input.file=file:/secure/input/products.csv
batch.output.file=file:/secure/output/sales_report.csv

# ファイル検証
batch.file.validation.enabled=true
batch.file.validation.max.size=100MB
```

### Actuatorセキュリティ

```properties
# セキュアなactuatorエンドポイント
management.endpoints.web.base-path=/management
management.security.enabled=true
management.endpoints.web.exposure.include=health,info
```

## 📊 監視とメトリクス

### カスタムメトリクス

```properties
# カスタムメトリクスを有効化
management.metrics.enable.batch=true
management.metrics.enable.jvm=true
management.metrics.enable.system=true

# メトリクスエクスポート
management.metrics.export.prometheus.enabled=true
management.metrics.export.influx.enabled=false
```

### ヘルスチェック

```properties
# ヘルスチェック設定
management.health.batch.enabled=true
management.health.db.enabled=true
management.health.diskspace.enabled=true
management.health.diskspace.threshold=100MB
```

## 🔄 ジョブ再起動設定

### 再起動パラメータ

```properties
# ジョブ再起動設定
spring.batch.job.restart.enabled=true
batch.job.restart.allow.start.if.complete=false

# ステップ再起動設定
batch.step.restart.allow=true
batch.step.restart.limit=3
```

### 再起動コマンド

```bash
# 失敗したジョブを再起動
java -jar product-data-etl.jar --spring.batch.job.names=productEtlJob --restart=true

# 特定のステップから再起動
java -jar product-data-etl.jar --restart.from.step=step2_generateReportFromDb
```

## 🧪 テスト設定

### テストプロパティ

```properties
# テスト固有の設定
spring.test.database.replace=none
spring.batch.job.enabled=true
batch.test.chunk.size=2
batch.test.skip.limit=1
logging.level.org.springframework.batch.test=DEBUG
```

### 統合テスト設定

```properties
# 統合テストデータベース
spring.datasource.url=jdbc:h2:mem:integrationtest;DB_CLOSE_DELAY=-1
spring.sql.init.mode=always
spring.batch.initialize-schema=always
```

この設定ガイドは、特定の要件に応じて製品データETLアプリケーションをカスタマイズおよびチューニングするためのすべての利用可能なオプションの包括的なカバレッジを提供します。