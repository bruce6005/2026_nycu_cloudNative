<pre>
# frontend 架構
src/
├── main.tsx                # React 入口點，負責掛載 App
├── App.tsx                 # 全域路由與頁面切換控制
├── App.css / index.css     # 全域樣式
│
├── layouts/
│   └── Layout.tsx          # 共用版型（Navbar / Sidebar / Page container）
│
├── features/               # 依功能切分（核心架構）
│   ├── order/              # 訂單功能模組
│   │   ├── api/
│   │   │   └── orderApi.ts     # 呼叫後端 API（fetch / axios）
│   │   ├── components/
│   │   │   ├── OrderForm.tsx   # 建立訂單表單 UI
│   │   │   └── OrderList.tsx   # 訂單列表 UI
│   │   ├── page/
│   │   │    └── OrderPage.tsx   # 頁面（整合 API + components）
│   │   └── style/
│   │       └── style.tsx   # 這個feature的css
│   │
│   └── approval/           # 審核功能模組（尚未實作）
│
├── assets/                 # 靜態資源（圖片 / icon）
├── config/                 # 設定檔（API URL 等）
├── styles/                 # 設定檔（API URL 等）  
│   └── global.css          # 共用的css 統一一下風格  
└── config.js               # 全域設定


# backend 架構
src/main/java/com/example/demo/
├── DemoApplication.java        # Spring Boot 啟動入口
│
├── config/
│   └── CorsConfig.java         # 跨域設定（允許前端呼叫 API）
│
├── modules/                    # 依功能切分（對應 frontend features）
│   ├── order/
│   │   ├── controller/
│   │   │   └── OrderController.java   # API 入口（處理 HTTP request）
│   │   ├── service/
│   │   │   └── OrderService.java      # 商業邏輯處理
│   │   ├── repository/
│   │   │   └── OrderRepository.java   # 透過 JPA 存取資料庫（CRUD）
│   │   ├── model/
│   │   │   └── Order.java             # Entity（對應資料表）
│   │   └── dto/
│   │       └── OrderDTO.java          # 資料傳輸格式
│   │
│   └── approval/               # 審核模組（尚未實作）


# 簡陋DB
Table: cus_orders
├── id      # 訂單 ID（Primary Key）
├── name    # 訂單名稱

- 預設使用 MySQL 作為資料庫
- 透過 JPA（Hibernate）自動建立資料表
- 若需更換資料庫再修改 application.properties 連線設定
</pre>

# 資料流
Frontend（前端 UI OrderPage）   
Order API（前端呼叫後端）    
Controller（接收 request）   
Service（處理商業邏輯）   
Repository（透過 JPA 存取資料）   
Database（CRUD）   


# 啟動流程（本地開發）

## 0. 前置需求

- Docker / Docker Compose
- Java 17
- Node.js / npm

## 1. 啟動資料庫（MySQL）

確認 Docker 已啟動後，在專案根目錄執行：

```bash
docker compose up -d
```

目前 `docker-compose.yml` 會建立：

- database: `lab_system`
- username: `root`
- password: `1234`
- port: `3306`

## 2. 設定 Frontend 環境變數

前端使用 Vite，環境變數必須以 `VITE_` 開頭。

先複製範本：

```bash
cd frontend
cp .env.example .env
```

本地開發可使用：

```env
VITE_GOOGLE_CLIENT_ID=你的_google_client_id
VITE_API_BASE=http://localhost:8080
```

注意：`frontend/.env` 只放本機設定與敏感資料，不要 commit。

## 3. 設定 Backend 環境變數

後端的預設值已經能連到本地 Docker MySQL，所以只要使用預設資料庫設定，可以直接啟動。

目前預設值等同於：

```env
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/lab_system
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=1234
SPRING_JPA_HIBERNATE_DDL_AUTO=create
GOOGLE_CLIENT_ID=
APP_CORS_ALLOWED_ORIGINS=http://localhost:*
```

如果要明確指定，請在啟動 backend 前用 shell export：

```bash
cd backend
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/lab_system
export SPRING_DATASOURCE_USERNAME=root
export SPRING_DATASOURCE_PASSWORD=1234
export SPRING_JPA_HIBERNATE_DDL_AUTO=create
export GOOGLE_CLIENT_ID=你的_google_client_id
export APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5174
```

也可以在 IntelliJ / VS Code 的 Run Configuration 裡設定同樣的環境變數。

`backend/.env.example` 是範本檔；Spring Boot 不會自動讀取 `.env`，所以實際執行時仍要透過 shell export、IDE Run Configuration，或部署平台的 Environment Variables 設定。

## 4. 啟動 Backend

```bash
cd backend
./mvnw spring-boot:run
```

Backend 預設會跑在：

```text
http://localhost:8080
```

## 5. 啟動 Frontend

```bash
cd frontend
npm install
npm run dev
```

Frontend 預設會跑在 Vite 顯示的網址，通常是：

```text
http://localhost:5173
```

## 6. 部署環境設定提醒

部署到 Cloud Run 或其他平台時，不要使用本地 `.env` 檔案。請改在平台的 Environment Variables 設定：

Backend：

```env
SPRING_DATASOURCE_URL=正式資料庫連線字串
SPRING_DATASOURCE_USERNAME=正式資料庫使用者
SPRING_DATASOURCE_PASSWORD=正式資料庫密碼
SPRING_JPA_HIBERNATE_DDL_AUTO=update
GOOGLE_CLIENT_ID=正式 Google OAuth Client ID
APP_CORS_ALLOWED_ORIGINS=https://你的前端網址
```

Frontend build 時：

```env
VITE_GOOGLE_CLIENT_ID=正式 Google OAuth Client ID
VITE_API_BASE=https://你的後端網址
```

## 7. 容器化建置（部署實驗）

Backend image：

```bash
docker build -t lab-system-backend ./backend
```

本地用容器跑 backend 時，可以連本機 Docker MySQL。Mac / Windows Docker Desktop 可使用 `host.docker.internal`：

```bash
docker run --rm -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/lab_system \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=1234 \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=create \
  -e GOOGLE_CLIENT_ID=你的_google_client_id \
  -e APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:8081 \
  lab-system-backend
```

Frontend image：

```bash
docker build \
  --build-arg VITE_API_BASE=http://localhost:8080 \
  --build-arg VITE_GOOGLE_CLIENT_ID=你的_google_client_id \
  -t lab-system-frontend ./frontend
```

本地用容器跑 frontend：

```bash
docker run --rm -p 8081:8080 lab-system-frontend
```

Frontend container 會由 nginx 服務靜態檔案，本地網址為：

```text
http://localhost:8081
```

Cloud Run 上兩個服務都會使用 container image 部署：

- backend image：runtime 由 Cloud Run 設定 DB / Google OAuth / CORS 環境變數
- frontend image：build image 時注入 `VITE_API_BASE` 與 `VITE_GOOGLE_CLIENT_ID`
