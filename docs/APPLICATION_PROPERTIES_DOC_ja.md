# application.properties 技術詳細解説

## 1. はじめに

`application.properties`ファイルは、Spring Bootアプリケーションの中核的な設定ファイルです。データベース接続からバッチジョブの挙動、ロギングレベルまで、すべての設定可能なパラメータを一元管理する場所を提供します。設定を外部化することにより、Javaコードを一切変更することなく、アプリケーションの調整、デプロイ、保守を容易に行うことができます。

このドキュメントでは、本プロジェクトで使用されているすべての設定項目を一つずつ解析します。

---

## 2. H2データベース設定

このセクションでは、開発およびテスト用に使用されるH2インメモリデータベースを設定します。

| プロパティキー | 設定例 | 説明 |
| :--- | :--- | :--- |
| `spring.datasource.url` | `jdbc:h2:mem:testdb` | H2データベースのJDBC接続URLです。`mem:testdb`は`testdb`という名前のインメモリデータベースであることを示し、アプリケーションが停止するとデータは失われます。 |
| `spring.datasource.driverClassName` | `org.h2.Driver` | H2データベースのJDBCドライバクラスです。 |
| `spring.datasource.username` | `sa` | データベース接続ユーザー名です。 |
| `spring.datasource.password` | `password` | データベース接続パスワードです。 |
| `spring.h2.console.enabled` | `true` | H2データベースのWebコンソールを有効にします。 |
| `spring.h2.console.path` | `/h2-console` | H2コンソールのアクセスパスで、`http://localhost:8080/h2-console`でアクセスできます。 |

---

## 3. JPA/Hibernate設定

このセクションでは、JPA（Java Persistence API）とその実装であるHibernateの動作を制御します。

| プロパティキー | 設定例 | 説明 |
| :--- | :--- | :--- |
| `spring.jpa.database-platform` | `org.hibernate.dialect.H2Dialect` | HibernateがH2に最適化されたSQLを生成するために使用すべきデータベース方言を指定します。 |
| `spring.jpa.hibernate.ddl-auto` | `none` | Hibernateのデータベーススキーマ生成戦略を制御します。`none`は本番環境でのベストプラクティスであり、Hibernateにテーブルの自動作成や変更をさせず、`schema.sql`などのスクリプトに依存させます。 |
| `spring.jpa.show-sql` | `true` | Hibernateが生成したSQL文をコンソールに出力し、デバッグに役立てます。 |
| `spring.jpa.properties.hibernate.format_sql` | `true` | 出力されるSQL文をフォーマットし、可読性を向上させます。 |
| `spring.sql.init.mode` | `always` | データベース初期化モードを設定します。`always`はアプリケーション起動時に常にSQLスクリプトを実行することを意味します。 |
| `spring.sql.init.schema-locations` | `classpath:schema.sql` | データベーススキーマを初期化するために使用されるSQLスクリプトの場所を指定します。`classpath:`はプロジェクトのリソースパスから探すことを示します。 |

---

## 4. Spring Batch設定

このセクションは、Spring Batchフレームワークの動作を設定するためのものです。

| プロパティキー | 設定例 | 説明 |
| :--- | :--- | :--- |
| `spring.batch.job.enabled` | `false` | アプリケーション起動時に定義済みのすべてのJobが自動実行されるのを禁止します。このプロジェクトでは、`CommandLineRunner`を介してJobの起動タイミングを正確に制御します。 |
| `spring.batch.initialize-schema` | `always` | Spring Batchメタデータテーブルの初期化を制御します。`always`は、ジョブ実行状態を保存するための`BATCH_*`シリーズのテーブルが起動のたびにチェックされ、作成されることを保証します。 |
| `spring.batch.job.names` | `productEtlJob` | （任意）コマンドラインから特定のジョブをトリガーするための名前です。 |

---

## 5. ロギング設定

このセクションでは、異なるコードパスにおけるログ出力レベルを細かく制御します。

| プロパティキー | 設定例 | 説明 |
| :--- | :--- | :--- |
| `logging.level.root` | `INFO` | ロギングシステムのルートレベルを`INFO`に設定します。明示的に指定されていないすべてのロガーはこのレベルを継承します。 |
| `logging.level.org.springframework.batch` | `INFO` | Spring Batchフレームワーク自体のログレベルを`INFO`に設定し、コアなイベントを監視します。 |
| `logging.level.com.example.productdataetl` | `DEBUG` | 私たちのアプリケーションコードのログレベルを`DEBUG`に設定し、詳細なデバッグ情報を取得します。 |
| `logging.level.com.example.productdataetl.listener` | `INFO` | リスナーのログレベルを`INFO`に設定し、標準実行モードでジョブとステップのライフサイクルイベントを明確に報告させます。 |
| `logging.level.com.example.productdataetl.config.CustomSkipPolicy` | `WARN` | スキップポリシーのログレベルを`WARN`に設定し、スキップが発生した場合にのみ警告情報を記録します。 |
| `logging.level.org.springframework.retry` | `DEBUG` | Spring Retryフレームワークの`DEBUG`ログを有効にし、詳細なリトライプロセスを観察します。 |
| `logging.level.org.hibernate.SQL` | `DEBUG` | Hibernateに実行されたSQL文を出力させます（`spring.jpa.show-sql=true`と同等）。 |
| `logging.level.org.hibernate.type.descriptor.sql.BasicBinder` | `TRACE` | これを`TRACE`レベルに設定すると、SQLのPreparedStatementにバインドされる実際のパラメータ値を確認でき、SQLデバッグの強力なツールとなります。 |

---

## 6. ファイルおよびバッチ処理設定

これらは、`@Value`アノテーションを介してJavaコードに注入される、カスタムのビジネスおよびパフォーマンス関連のパラメータです。

| プロパティキー | 設定例 | 説明 |
| :--- | :--- | :--- |
| `batch.input.file` | `classpath:products.csv` | 入力CSVファイルの場所を定義します。 |
| `batch.output.file` | `file:sales_report.csv` | 出力レポートファイルの場所を定義します。`file:`はプロジェクトルートからの相対的なファイルシステムパスを示します。 |
| `batch.csv.skip.lines` | `1` | CSV読み取り時に最初の行（通常はヘッダー）をスキップします。 |
| `batch.chunk.size` | `10` | バッチ処理の「チャンクサイズ」、つまり各トランザクションで処理されるレコード数を定義します。これはパフォーマンスとメモリ使用量に影響を与える重要なパラメータです。 |
| `batch.page.size` | `100` | `JpaPagingItemReader`がデータベースからデータをページングで読み取る際の各ページのサイズです。 |
| `batch.skip.limit` | `5` | Stepの実行中にスキップできるレコードの最大数です。この制限を超えると、Stepは失敗します。 |
| `batch.retry.limit` | `3` | リトライ可能な一時的な例外に対する最大リトライ回数です。 |

---

## 7. 監視および管理設定

このセクションでは、アプリケーションの監視と管理のためにSpring Boot ActuatorとJMXを設定します。

| プロパティキー | 設定例 | 説明 |
| :--- | :--- | :--- |
| `spring.jmx.enabled` | `true` | Java Management Extensions (JMX)を有効にし、JMXクライアント（JConsoleなど）を介したアプリケーションの監視と管理を許可します。 |
| `management.endpoints.web.exposure.include` | `health,info,metrics,beans` | ActuatorのエンドポイントをHTTP経由で公開します。`health`は健康状態をチェックし、`info`はアプリケーション情報を表示し、`metrics`はメトリクスを提供し、`beans`はすべてのSpring Beanを表示します。 |
| `management.endpoint.health.show-details` | `always` | `/actuator/health`エンドポイントにアクセスした際に、常に詳細な健康情報を表示します。 |
| `info.app.name` | `Product Data ETL` | `/actuator/info`エンドポイントに表示されるカスタムアプリケーション情報です。 |
