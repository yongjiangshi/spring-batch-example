# ジョブ実行クイックリファレンスガイド

このガイドは、様々なパラメータと設定で製品データETLジョブを実行するためのクイックリファレンスを提供します。

## 🚀 クイックスタートコマンド

### 基本実行
```bash
# デフォルト設定で実行
mvn spring-boot:run

# パッケージ化されたJARを実行
java -jar target/product-data-etl-0.0.1-SNAPSHOT.jar
```

### 一般的な実行シナリオ

#### 1. カスタム入力/出力ファイル
```bash
# カスタムCSV入力ファイル
java -jar product-data-etl.jar \
  --batch.input.file=file:/path/to/custom-products.csv

# カスタム出力場所
java -jar product-data-etl.jar \
  --batch.output.file=file:/path/to/custom-report.csv

# カスタム入力と出力の両方
java -jar product-data-etl.jar \
  --batch.input.file=file:/data/products.csv \
  --batch.output.file=file:/reports/sales_report.csv
```

#### 2. パフォーマンスチューニング
```bash
# 大きなデータセット処理
java -Xmx4g -jar product-data-etl.jar \
  --batch.chunk.size=1000 \
  --batch.page.size=1000

# 詳細ログ付きの小さなデータセット
java -jar product-data-etl.jar \
  --batch.chunk.size=5 \
  --logging.level.com.example.productdataetl=DEBUG
```

#### 3. エラーハンドリングのカスタマイゼーション
```bash
# 厳格なエラーハンドリング（フェイルファスト）
java -jar product-data-etl.jar \
  --batch.skip.limit=0 \
  --batch.retry.limit=1

# 寛容なエラーハンドリング
java -jar product-data-etl.jar \
  --batch.skip.limit=100 \
  --batch.retry.limit=5 \
  --batch.error.log.skipped.items=true
```

#### 4. ビジネスロジックのカスタマイゼーション
```bash
# 売上レポートの異なる価格閾値
java -jar product-data-etl.jar \
  --batch.sales.report.price.threshold=75.0

# カスタム日付形式
java -jar product-data-etl.jar \
  --batch.date.format="dd/MM/yyyy HH:mm:ss"
```

## 📋 パラメータリファレンス

### ファイル処理パラメータ

| パラメータ | デフォルト | 説明 | 例 |
|-----------|----------|------|-----|
| `batch.input.file` | classpath:products.csv | 入力CSVファイルの場所 | `file:/data/products.csv` |
| `batch.output.file` | file:sales_report.csv | 出力レポートファイルの場所 | `file:/reports/sales.csv` |
| `batch.csv.delimiter` | , | CSVフィールド区切り文字 | `;` または `\|` |
| `batch.csv.skip.lines` | 1 | スキップするヘッダー行数 | `0` または `2` |

### 処理パラメータ

| パラメータ | デフォルト | 説明 | 例 |
|-----------|----------|------|-----|
| `batch.chunk.size` | 10 | トランザクションチャンクあたりのアイテム数 | `100` または `1000` |
| `batch.page.size` | 100 | データベースページネーションサイズ | `500` または `1000` |
| `batch.thread.pool.size` | 1 | コアスレッドプールサイズ | `2` または `4` |

### エラーハンドリングパラメータ

| パラメータ | デフォルト | 説明 | 例 |
|-----------|----------|------|-----|
| `batch.skip.limit` | 5 | スキップする最大アイテム数 | `0`（フェイルファスト）または `100` |
| `batch.retry.limit` | 3 | 最大リトライ回数 | `1` または `10` |
| `batch.retry.initial.delay` | 1000 | 初期リトライ遅延（ミリ秒） | `500` または `2000` |
| `batch.error.log.skipped.items` | true | スキップされたアイテムをログ出力 | `false` |

### ビジネスロジックパラメータ

| パラメータ | デフォルト | 説明 | 例 |
|-----------|----------|------|-----|
| `batch.sales.report.price.threshold` | 50.0 | 価格フィルター閾値 | `25.0` または `100.0` |
| `batch.date.format` | yyyy-MM-dd HH:mm:ss | インポート日付形式 | `dd/MM/yyyy` |

## 🔧 環境固有の実行

### 開発環境
```bash
java -jar product-data-etl.jar \
  --spring.profiles.active=dev \
  --logging.level.com.example.productdataetl=DEBUG \
  --batch.chunk.size=5
```

### テスト環境
```bash
java -jar product-data-etl.jar \
  --spring.profiles.active=test \
  --batch.input.file=classpath:test-products.csv \
  --batch.chunk.size=2
```

### 本番環境
```bash
java -Xmx2g -jar product-data-etl.jar \
  --spring.profiles.active=prod \
  --batch.chunk.size=1000 \
  --batch.page.size=1000 \
  --logging.level.root=WARN
```

## 🔄 ジョブ再起動と回復

### 失敗したジョブの再起動
```bash
# 同じパラメータで再起動
java -jar product-data-etl.jar \
  --spring.batch.job.names=productEtlJob \
  --restart=true

# 異なるパラメータで再起動
java -jar product-data-etl.jar \
  --spring.batch.job.names=productEtlJob \
  --restart=true \
  --batch.chunk.size=50
```

### 一意実行のためのジョブパラメータ
```bash
# 一意のジョブインスタンスのためのタイムスタンプ追加
java -jar product-data-etl.jar \
  --job.parameters="timestamp=$(date +%s)"

# カスタム実行識別子を追加
java -jar product-data-etl.jar \
  --job.parameters="runId=manual-$(date +%Y%m%d-%H%M%S)"
```

## 📊 監視とデバッグ

### デバッグモード
```bash
# デバッグログを有効化
java -jar product-data-etl.jar \
  --logging.level.com.example.productdataetl=DEBUG \
  --logging.level.org.springframework.batch=DEBUG

# SQLクエリログ
java -jar product-data-etl.jar \
  --spring.jpa.show-sql=true \
  --logging.level.org.hibernate.SQL=DEBUG
```

### パフォーマンス監視
```bash
# JMX監視を有効化
java -Dcom.sun.management.jmxremote \
  -Dcom.sun.management.jmxremote.port=9999 \
  -Dcom.sun.management.jmxremote.authenticate=false \
  -Dcom.sun.management.jmxremote.ssl=false \
  -jar product-data-etl.jar

# actuatorエンドポイントを有効化
java -jar product-data-etl.jar \
  --management.endpoints.web.exposure.include=health,metrics,batch
```

## 🚨 トラブルシューティングコマンド

### ファイル問題
```bash
# ファイル権限を確認
ls -la /path/to/input/products.csv

# 絶対パスでテスト
java -jar product-data-etl.jar \
  --batch.input.file=file:/absolute/path/to/products.csv
```

### メモリ問題
```bash
# ヒープサイズを増やす
java -Xmx4g -jar product-data-etl.jar

# GCログを有効化
java -XX:+PrintGC -XX:+PrintGCDetails \
  -Xloggc:gc.log \
  -jar product-data-etl.jar
```

### データベース問題
```bash
# デバッグ用にH2コンソールを有効化
java -jar product-data-etl.jar \
  --spring.h2.console.enabled=true

# 外部データベースでテスト
java -jar product-data-etl.jar \
  --spring.datasource.url=jdbc:postgresql://localhost:5432/testdb \
  --spring.datasource.username=testuser \
  --spring.datasource.password=testpass
```

## 📝 実行スクリプトの例

### Linux/Macスクリプト（run.sh）
```bash
#!/bin/bash

# 変数を設定
JAR_FILE="target/product-data-etl-0.0.1-SNAPSHOT.jar"
INPUT_FILE="/data/products.csv"
OUTPUT_FILE="/reports/sales_report_$(date +%Y%m%d).csv"

# ジョブを実行
java -Xmx2g -jar "$JAR_FILE" \
  --batch.input.file="file:$INPUT_FILE" \
  --batch.output.file="file:$OUTPUT_FILE" \
  --batch.chunk.size=500 \
  --job.parameters="timestamp=$(date +%s)"

echo "ジョブが完了しました。レポートは次の場所に保存されました：$OUTPUT_FILE"
```

### Windowsスクリプト（run.bat）
```batch
@echo off

set JAR_FILE=target\product-data-etl-0.0.1-SNAPSHOT.jar
set INPUT_FILE=C:\data\products.csv
set OUTPUT_FILE=C:\reports\sales_report_%date:~-4,4%%date:~-10,2%%date:~-7,2%.csv

java -Xmx2g -jar "%JAR_FILE%" ^
  --batch.input.file="file:%INPUT_FILE%" ^
  --batch.output.file="file:%OUTPUT_FILE%" ^
  --batch.chunk.size=500

echo ジョブが完了しました。レポートは次の場所に保存されました：%OUTPUT_FILE%
```

このクイックリファレンスガイドは、様々なシナリオで製品データETLジョブを効果的に実行するために必要なすべての基本的なコマンドとパラメータを提供します。