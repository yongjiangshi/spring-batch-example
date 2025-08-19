### **ユーザーストーリー：製品データETLとレポート生成**

**私は** Spring Batchを深く学習したいソフトウェア開発者として、
**私は** 最初に製品情報をCSVファイルからデータベースにインポートし、次にこのデータをデータベースから読み取り、フィルタリングと処理を行い、最終的に売上レポートを生成して新しいCSVファイルとして保存するマルチステップバッチジョブを構築したい、
**そうすることで** 複数のステップ（Step）を含む複雑なジョブ（Job）を編成する方法を習得し、ファイルベースとデータベースベースの`ItemReader`と`ItemWriter`を同時に使用する方法を学ぶことができる。

---

### **受け入れ基準（Acceptance Criteria）：**

#### **第1部：一般的なセットアップ**

1.  **プロジェクト依存関係：**
    *   [ ] アプリケーションは`spring-boot-starter-batch`、`spring-boot-starter-data-jpa`、`h2database`依存関係を含むSpring Bootプロジェクトである。
2.  **データベース：**
    *   [ ] テストと実行を容易にするためにH2インメモリデータベースを使用する。
    *   [ ] プロジェクト起動時に、`schema.sql`を通じて`id`、`name`、`description`、`price`フィールドを含む`PRODUCTS`という名前のテーブルを自動作成する。
3.  **バッチジョブ（Job）：**
    *   [ ] `productEtlJob`という名前の`Job`を定義する。
    *   [ ] この`Job`は順次実行される2つの`Step`を含む：`step1_loadCsvToDb`と`step2_generateReportFromDb`。

---

#### **第2部：ステップ1（Step 1）- CSVからデータベースへのインポート**

4.  **入力データ：**
    *   [ ] プロジェクトには少なくとも`id`、`name`、`description`、`price`列と10件以上の製品データを含む`products.csv`ファイルが含まれている。
5.  **データ読み取り（Reader）：**
    *   [ ] `products.csv`を読み取り、`Product` JPAエンティティオブジェクトにマッピングする`FlatFileItemReader`を実装する。
6.  **データ処理（Processor）：**
    *   [ ] （デモンストレーション用、オプション）データのクリーニングや変換を行う`ItemProcessor`を実装する（例：デフォルトの`importDate`フィールドを設定）。
7.  **データ書き込み（Writer）：**
    *   [ ] `Product`エンティティオブジェクトをH2データベースの`PRODUCTS`テーブルにバッチ保存する`JpaItemWriter`を実装する。
8.  **検証：**
    *   [ ] `Step 1`が正常に実行された後、`PRODUCTS`テーブルには`products.csv`ファイルの内容と一致するデータが含まれている必要がある。

---

#### **第3部：ステップ2（Step 2）- データベースからレポートCSVを生成**

9.  **データ読み取り（Reader）：**
    *   [ ] `PRODUCTS`テーブルから`Product`エンティティをページネーションで読み取る`JpaPagingItemReader`を実装する。
10. **データ処理（Processor）：**
    *   [ ] データのフィルタリングと変換を行う`ItemProcessor`を実装する。例えば、`price`が50より大きいすべての製品をフィルタリングし、新しい`SalesReport` DTOオブジェクトに変換する。
11. **データ書き込み（Writer）：**
    *   [ ] `SalesReport` DTOオブジェクトを`sales_report.csv`という名前の新しいファイルに書き込む`FlatFileItemWriter`を実装する。ファイルの列は`productId`、`productName`、`price`とする。
12. **検証：**
    *   [ ] `Step 2`が正常に実行された後、プロジェクトルートディレクトリに`sales_report.csv`ファイルが生成される。
    *   [ ] `sales_report.csv`ファイル内のデータエントリは、データベース内で`price`が50より大きい製品のみを含む必要がある。