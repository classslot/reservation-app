# 予約管理システム

> 振替レッスンとイベントの空き枠確認・予約を簡単にできるWebアプリ

スクールの **保護者（会員）** が、スマートフォンから自分の子どもに合う空き枠を確認して予約・取消でき、**スタッフ** が生徒情報・予約枠・全予約を一元管理できる予約管理アプリです。

## 開発の背景

<!-- TODO: あなた自身の言葉で書き直してください（評価観点「開発の背景」）。下は企画書から起こした下書きです -->
- 対象: スクールの運営スタッフと、受講生の保護者
- 課題: 振替レッスン・イベントの日程調整を個別の連絡で行っており、予約情報が複数の場所に分散して転記・確認の手間がかかる。定員超過や同じ枠の重複予約も起こりうる。
- 解決: 空き枠の確認から予約・取消までをWeb上で完結させ、生徒情報・予約枠・予約を一元管理する。定員超過と重複予約はサーバー側で防ぐ。

## アプリケーションのURL

<!-- デプロイした場合のみ記載（未デプロイなら、この節ごと削除） -->
- URL: （デプロイ後に記載）
- テストアカウント
  | 種別 | ログインID | パスワード |
  |---|---|---|
  | スタッフ | `staff` | `staff1234` |
  | 会員 | `member1` | `pass1234` |

## デモ（画面イメージ）

<!-- TODO: 実際に起動して撮影したスクリーンショットを docs/images/ に置き、下を差し替えてください -->
| 画面 | 説明 |
|---|---|
| ![ログイン](docs/images/demo-login.png) | ログイン認証（会員／スタッフで遷移先が変わる） |
| ![予約枠一覧](docs/images/demo-slots.png) | 自分が対象の空き枠と残席を確認（会員） |
| ![予約管理](docs/images/demo-reservations.png) | 全予約・参加者の確認と取消（スタッフ） |

画面遷移図・画面設計書は [docs/images/screen-transition.png](docs/images/screen-transition.png) / [docs/images/screen-design.png](docs/images/screen-design.png) を参照してください。

## 主な機能

| 利用者 | 機能 |
|---|---|
| 共通 | ログイン／ログアウト（セッション認証）、未ログイン時は画面・APIとも利用不可 |
| 会員 | 対象の空き枠一覧（種別・日付で絞込）、枠詳細、予約、自分の予約一覧、予約取消、登録情報の確認 |
| スタッフ | 生徒の登録・検索・詳細・編集・退会、予約枠の登録・編集・受付停止、全予約・参加者の確認、予約取消 |

予約のルール: 対象コース／ゲーム／レベルに合う枠のみ・振替許可のある生徒のみ・満席／受付停止／開始済みの枠は不可・同じ枠の重複予約は不可・取消は開始前のみ。

## 設計上のポイント

- **定員超過を防ぐ**: 予約時に枠を行ロック（悲観的ロック）して確定予約数を数え直し、同時に予約されても定員を超えない
- **残席は保存しない**: `capacity` から確定予約数を引いて計算し、二重管理による不整合を避ける
- **権限はサーバー側で確認**: 画面の出し分けだけでなく、Spring Securityで会員／スタッフのAPIを分離。会員向けAPIは他の生徒の情報を返さない
- **履歴を残す**: 退会・予約取消は削除せず状態変更。取消済みの枠を再予約するときは同じ予約行を更新（`student_id + slot_id` を一意制約に）
- **パスワードはBCryptでハッシュ化**して保存し、APIでは返さない
- **エラーは仕様どおりのステータス**（401/403/404/409/422）とメッセージで返し、画面に理由を表示

### 苦労した点・つまずいたところ

<!-- TODO: 開発中に実際につまずいたこと・どう解決したかをあなた自身の言葉で書いてください（評価対象） -->

## 使用技術

| Category | Technology Stack |
|---|---|
| Frontend | JavaScript, React 18, Vite, React Router |
| Backend | Java 21, Spring Boot 3.3, Spring Data JPA, Spring Web |
| Authentication | Spring Security（セッション認証）, BCrypt |
| Database | H2（ローカル開発）／ PostgreSQL（公開時: Neon 等） |
| Infrastructure | Render（Backend）, Vercel または Netlify（Frontend） ※公開する場合 |
| Build tool | Gradle, npm |
| Test | JUnit 5, MockMvc |
| Design | draw.io |
| etc. | Git, GitHub, Postman |

## ER図

```mermaid
erDiagram
    users ||--o| students : "1 : 0..1 (users.id = students.user_id)"
    students ||--o{ reservations : "1 : 0..N"
    slots ||--o{ reservations : "1 : 0..N"

    users {
        bigint id PK
        varchar login_id UK
        varchar password_hash
        varchar role "MEMBER / STAFF"
        boolean is_active
        datetime created_at
        datetime updated_at
    }
    students {
        bigint id PK
        bigint user_id FK,UK
        varchar name
        varchar guardian_name
        varchar contact_email
        varchar course
        varchar game
        varchar level
        boolean makeup_allowed
        boolean is_active
        datetime created_at
        datetime updated_at
    }
    slots {
        bigint id PK
        varchar type "MAKEUP / EVENT"
        varchar title
        varchar teacher_name
        datetime start_at
        datetime end_at
        varchar target_course
        varchar target_game
        varchar target_level
        int capacity
        varchar status "OPEN / CLOSED"
        datetime created_at
        datetime updated_at
    }
    reservations {
        bigint id PK
        bigint student_id FK
        bigint slot_id FK
        varchar status "CONFIRMED / CANCELLED"
        datetime reserved_at
        datetime cancelled_at
        datetime created_at
        datetime updated_at
    }
```

`reservations` は `(student_id, slot_id)` の組合せを一意とします。ER図の元データ: [docs/images/er-diagram.png](docs/images/er-diagram.png)

## 構成図

```mermaid
flowchart LR
    U["利用者<br/>PC・スマートフォンのブラウザ"] -->|HTTPS| F["Frontend<br/>React + Vite<br/>(Vercel / Netlify)"]
    F -->|"/api/* を転送（同一オリジン）"| B["Backend<br/>Spring Boot (Java 21)<br/>(Render)"]
    B -->|JPA / JDBC| D[("Database<br/>PostgreSQL (Neon)<br/>ローカルは H2")]
```

## API一覧

| 機能 | メソッド | URL | 権限 |
|---|---|---|---|
| ログイン / ログアウト | POST | `/api/login` / `/api/logout` | 不要 / ログイン済み |
| ログイン利用者取得 | GET | `/api/me` | ログイン済み |
| 生徒 一覧・詳細・登録・更新(退会) | GET / GET / POST / PATCH | `/api/students`, `/api/students/{id}` | スタッフ |
| 予約枠 一覧・詳細 | GET | `/api/slots`, `/api/slots/{id}` | ログイン済み（会員は対象枠のみ） |
| 予約枠 登録・更新(受付停止) | POST / PATCH | `/api/slots`, `/api/slots/{id}` | スタッフ |
| 予約確定 | POST | `/api/reservations` | 会員 |
| 自分の予約 一覧・取消 | GET / POST | `/api/me/reservations`, `/api/me/reservations/{id}/cancel` | 会員 |
| 全予約・参加者 / 予約取消 | GET / POST | `/api/reservations`, `/api/reservations/{id}/cancel` | スタッフ |

詳細は [docs/design/](docs/design/) のAPI仕様書（xlsx）を参照してください。

## ローカル環境での起動方法

### 必要環境

- Java 21
- Node.js 20 以上
- Git
- Gradle 8.x（IDE（IntelliJ等）から起動する場合は不要）

### 1. リポジトリを取得

```bash
git clone <このリポジトリのURL>
cd reservation-app
```

### 2. バックエンド（Spring Boot）を起動

DBは **H2（ファイル保存）** を使うため、DBの事前準備は不要です。初回起動時にテーブルと動作確認用データが自動作成されます。

```bash
cd backend
gradle bootRun
# → http://localhost:8080 で起動します
```

> Gradle Wrapper を使う場合は、最初に一度 `gradle wrapper --gradle-version 8.10` を実行すると `./gradlew bootRun` が使えます。
> テストは `gradle test`（ログイン → 予約 → 重複不可 → 取消 → 権限チェックの流れを確認）。

### 3. フロントエンド（React）を起動（別のターミナル）

```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173 を開きます（/api は自動でバックエンドへ転送）
```

### 4. ログインして動作確認

| 種別 | ログインID | パスワード | 備考 |
|---|---|---|---|
| スタッフ | `staff` | `staff1234` | 生徒・予約枠・予約の管理 |
| 会員 | `member1` | `pass1234` | ベーシック／ゲームA／初級・振替可 |
| 会員 | `member2` | `pass1234` | アドバンス／ゲームB／中級・振替可 |
| 会員 | `member3` | `pass1234` | 振替不可（振替枠が表示されないことを確認できる） |

データを初期状態に戻すには、バックエンドを止めて `backend/data/` を削除し、再起動します。

## 公開（デプロイ）手順の例

無料枠で完結する構成の一例です（各サービスの最新の無料枠条件は公式サイトで確認してください）。

1. **DB（Neon）**: PostgreSQL を作成し、接続文字列（`jdbc:postgresql://<host>/<db>?sslmode=require`）・ユーザー・パスワードを控える
2. **バックエンド（Render）**: GitHubリポジトリから Web Service を作成（Runtime: Docker、Root Directory: `backend`）。環境変数を設定
   - `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` … Neonの接続情報
   - `SEED_ENABLED=true` … 初回のみテストデータを作成（不要になったら `false`）
   - `COOKIE_SECURE=true`
3. **フロントエンド（Vercel または Netlify）**: Root Directory を `frontend` に設定。`frontend/vercel.json`（Netlifyは `frontend/public/_redirects`）内の `BACKEND_URL` を RenderのURLのホスト名に書き換える。`/api/*` をバックエンドへ転送するため、ブラウザからは同一オリジンとなりCookie認証がそのまま動く
4. READMEの「アプリケーションのURL」にURLとテストアカウントを記載する

※ 無料枠のバックエンドはアクセスがないとスリープし、最初のアクセスに1分ほどかかることがあります。

## 今後の改善案

- CSRF対策（現状は学習用としてトークンを無効化し、SameSite Cookieで代替）
- 決済・自動通知・外部カレンダー連携・パスワード再発行（要件定義で範囲外とした機能）
- ログインIDやパスワードの変更機能、兄弟の一括管理

## ディレクトリ構成

```
reservation-app/
├── backend/    Spring Boot（controller / service / repository / domain / dto / config）
├── frontend/   React + Vite（pages / components / lib）
└── docs/       設計資料（要件定義・API仕様書・ER図・画面遷移図・画面設計書）
```
