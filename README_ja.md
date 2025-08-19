# 製品データETL - Spring Batchアプリケーション

高度なバッチ処理機能を実証する包括的なSpring Batch ETL（抽出、変換、ロード）パイプライン。このアプリケーションは、マルチステップワークフローを通じて製品データを処理します：CSVデータをデータベースにインポートし、フィルタリングされた売上レポートを生成します。

## 🚀 機能

- **マルチステップバッチ処理**：適切なシーケンシングを持つ2ステップETLパイプライン
- **ファイルからデータベースへの処理**：データ検証と変換を伴うCSVインポート
- **データベースからファイルへの処理**：カスタムビジネスロジックによるフィルタリングされたレポート生成
- **堅牢なエラーハンドリング**：スキップポリシー、リトライメカニズム、包括的なログ記録
- **Spring Boot統合**：自動設定を持つモダンなSpring Bootアプリケーション
- **インメモリデータベース**：開発とテストを容易にするH2データベース
- **包括的なテスト**：高いカバレッジを持つユニットテストと統合テスト

## 📋 前提条件

- Java 11以上
- Maven 3.6+またはGradle 6+
- IDE（IntelliJ IDEA、Eclipse、またはVS Codeを推奨）

## 🛠️ セットアップ手順

### 1. クローンとビルド

```bash
# リポジトリをクローン
git clone <repository-url>
cd product-data-etl

# プロジェクトをビルド
mvn clean compile

# テストを実行
mvn test

# アプリケーションをパッケージ化
mvn package
```

### 2. データベースセットアップ

アプリケーションは自動設定されるH2インメモリデータベースを使用します。追加のセットアップは不要です。

**H2コンソールアクセス：**
- URL：http://localhost:8080/h2-console
- JDBC URL：`jdbc:h2:mem:testdb`
- ユーザー名：`sa`
- パスワード：`password`

### 3. 入力データの準備

CSVファイルを`src/main/resources/products.csv`に以下の形式で配置してください：

```csv
id,name,description,price
1,ラップトップ,高性能ラップトップ,999.99
2,マウス,ワイヤレスマウス,25.50
3,キーボード,メカニカルキーボード,75.00
```

## 🏃‍♂️ アプリケーションの実行

### 標準実行

```bash
# 完全なETLパイプラインを実行
mvn spring-boot:run

# またはJARファイルを実行
java -jar target/product-data-etl-1.0.0.jar
```

### カスタム設定

```bash
# カスタム入力ファイルで実行
java -jar target/product-data-etl-1.0.0.jar --batch.input.file=file:/path/to/custom.csv

# カスタム出力場所で実行
java -jar target/product-data-etl-1.0.0.jar --batch.output.file=file:/path/to/custom_report.csv

# カスタムチャンクサイズで実行
java -jar target/product-data-etl-1.0.0.jar --batch.chunk.size=50
```

### 開発モード

```bash
# デバッグログで実行
java -jar target/product-data-etl-1.0.0.jar --logging.level.com.example.productdataetl=DEBUG

# SQLログを有効にして実行
java -jar target/product-data-etl-1.0.0.jar --spring.jpa.show-sql=true
```

## 📊 ジョブ実行フロー

### ステップ1：CSVからデータベースへのインポート
1. **読み取り**：`FlatFileItemReader`を使用してCSVファイルから製品を読み取り
2. **処理**：インポートタイムスタンプを追加してデータを検証
3. **書き込み**：`JpaItemWriter`を使用してPRODUCTSテーブルに永続化

### ステップ2：データベースからレポート生成
1. **読み取り**：`JpaPagingItemReader`を使用してデータベースから製品を読み取り
2. **処理**：価格が50円を超える製品をフィルタリングしてレポート形式に変換
3. **書き込み**：`FlatFileItemWriter`を使用して売上レポートCSVを生成

## 📁 プロジェクト構造

```
src/
├── main/
│   ├── java/com/example/productdataetl/
│   │   ├── config/           # バッチ設定クラス
│   │   ├── dto/              # データ転送オブジェクト
│   │   ├── exception/        # カスタム例外
│   │   ├── listener/         # ジョブとステップリスナー
│   │   ├── model/            # JPAエンティティ
│   │   ├── processor/        # アイテムプロセッサー
│   │   ├── reader/           # アイテムリーダー
│   │   └── writer/           # アイテムライター
│   └── resources/
│       ├── application.properties  # 設定
│       ├── schema.sql             # データベーススキーマ
│       └── products.csv           # サンプル入力データ
└── test/
    ├── java/                 # ユニットテストと統合テスト
    └── resources/
        └── test-products.csv # テストデータ
```

## ⚙️ 設定オプション

### コアバッチ設定

| プロパティ | デフォルト | 説明 |
|-----------|----------|------|
| `batch.chunk.size` | 10 | トランザクションごとに処理されるアイテム数 |
| `batch.page.size` | 100 | データベースページネーションサイズ |
| `batch.skip.limit` | 5 | ジョブ失敗前の最大スキップアイテム数 |
| `batch.retry.limit` | 3 | エラーの最大リトライ回数 |

### ファイル処理

| プロパティ | デフォルト | 説明 |
|-----------|----------|------|
| `batch.input.file` | classpath:products.csv | 入力CSVファイルの場所 |
| `batch.output.file` | file:sales_report.csv | 出力レポートファイルの場所 |
| `batch.csv.delimiter` | , | CSVフィールド区切り文字 |
| `batch.csv.skip.lines` | 1 | スキップするヘッダー行数 |

### ビジネスロジック

| プロパティ | デフォルト | 説明 |
|-----------|----------|------|
| `batch.sales.report.price.threshold` | 50.0 | 売上レポートの価格フィルター |
| `batch.date.format` | yyyy-MM-dd HH:mm:ss | インポート日付形式 |

### エラーハンドリング

| プロパティ | デフォルト | 説明 |
|-----------|----------|------|
| `batch.retry.initial.delay` | 1000 | 初期リトライ遅延（ミリ秒） |
| `batch.retry.max.delay` | 10000 | 最大リトライ遅延（ミリ秒） |
| `batch.retry.multiplier` | 2.0 | リトライ遅延倍率 |

## 🔍 監視とデバッグ

### アプリケーションログ

```bash
# アプリケーションログを表示
tail -f logs/application.log

# バッチ固有のログをフィルター
grep "BATCH" logs/application.log
```

### ジョブ実行監視

アプリケーションは以下の詳細なログ記録を提供します：
- ジョブ開始/完了ステータス
- ステップ実行進捗
- アイテム処理統計
- エラー詳細と回復アクション
- パフォーマンスメトリクス

### ヘルスチェック

ヘルスエンドポイントにアクセス：
- ヘルス：http://localhost:8080/actuator/health
- メトリクス：http://localhost:8080/actuator/metrics
- 情報：http://localhost:8080/actuator/info

## 🧪 テスト

### すべてのテストを実行

```bash
# ユニットテストのみ
mvn test

# 統合テストのみ
mvn test -Dtest="*IntegrationTest"

# 特定のテストクラス
mvn test -Dtest="ProductProcessorTest"
```

### テストカテゴリ

- **ユニットテスト**：個別コンポーネントテスト
- **統合テスト**：ステップとジョブレベルテスト
- **エンドツーエンドテスト**：完全なパイプライン検証

## 🚨 トラブルシューティング

### よくある問題

#### ジョブ開始失敗
```
エラー：ジョブ'productEtlJob'の開始に失敗しました
解決策：入力ファイルが存在し、読み取り可能であることを確認してください
```

#### データベース接続問題
```
エラー：H2データベースに接続できません
解決策：H2依存関係が含まれ、設定が正しいことを確認してください
```

#### ファイルが見つからない
```
エラー：入力ファイルが見つかりません
解決策：batch.input.fileプロパティのファイルパスを確認してください
```

#### メモリ問題
```
エラー：処理中にOutOfMemoryError
解決策：batch.chunk.sizeを減らすかJVMヒープサイズを増やしてください
```

### デバッグモード

詳細なトラブルシューティングのためにデバッグログを有効にする：

```properties
logging.level.com.example.productdataetl=DEBUG
logging.level.org.springframework.batch=DEBUG
```

## 📈 パフォーマンスチューニング

### チャンクサイズ最適化

データに基づいて異なるチャンクサイズをテスト：
- 小さなデータセット（< 1Kレコード）：chunk.size = 10-50
- 中程度のデータセット（1K-10Kレコード）：chunk.size = 100-500
- 大きなデータセット（> 10Kレコード）：chunk.size = 1000+

### メモリ管理

```bash
# 大きなデータセットのためにヒープサイズを増やす
java -Xmx2g -jar target/product-data-etl-1.0.0.jar

# GCログを有効にする
java -XX:+PrintGC -XX:+PrintGCDetails -jar target/product-data-etl-1.0.0.jar
```

## 🔧 カスタマイゼーション

### 新しい処理ステップの追加

1. 新しいItemReader、ItemProcessor、ItemWriterを作成
2. BatchConfigurationでステップを設定
3. ジョブフローにステップを追加
4. テストを更新

### カスタムビジネスロジック

特定のビジネスルールを実装するためにプロセッサーを変更：
- データ検証
- 変換ロジック
- フィルタリング条件

### 異なるデータソース

H2を本番データベースに置き換え：
- PostgreSQL
- MySQL
- Oracle
- SQL Server

## 📚 追加リソース

- [Spring Batchドキュメント](https://spring.io/projects/spring-batch)
- [Spring Bootリファレンスガイド](https://spring.io/projects/spring-boot)
- [H2データベースドキュメント](http://www.h2database.com/html/main.html)

## 🤝 貢献

1. リポジトリをフォーク
2. 機能ブランチを作成
3. 新機能のテストを追加
4. すべてのテストが通ることを確認
5. プルリクエストを提出

## 📄 ライセンス

このプロジェクトはMITライセンスの下でライセンスされています - 詳細についてはLICENSEファイルを参照してください。