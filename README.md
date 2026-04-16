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
│   │   └── page/
│   │       └── OrderPage.tsx   # 頁面（整合 API + components）
│   │
│   └── approval/           # 審核功能模組（尚未實作）
│
├── assets/                 # 靜態資源（圖片 / icon）
├── config/                 # 設定檔（API URL 等）
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


# DB（資料庫）
Table: cus_orders
├── id      # 訂單 ID（Primary Key）
├── name    # 訂單名稱

- 預設使用 MySQL 作為資料庫
- 透過 JPA（Hibernate）自動建立資料表
- 若需更換資料庫，請修改 application.properties 連線設定
</pre>

# 資料流
Frontend（前端 UI OrderPage）
    ↓
Order API（前端呼叫後端）
    ↓
Controller（接收 request）
    ↓
Service（處理商業邏輯）
    ↓
Repository（透過 JPA 存取資料）
    ↓
Database（CRUD）


# 啟動流程
# 1. 啟動資料庫（MySQL）
# 先確認 application.properties 連線設定正確

# 2. 啟動 Backend
cd backend
./mvnw spring-boot:run

# 3. 啟動 Frontend
cd frontend
npm install
npm run dev

# 啟動流程
# 1. 啟動資料庫（MySQL）
# 先確認 application.properties 連線設定正確

# 2. 啟動 Backend
cd backend
./mvnw spring-boot:run

# 3. 啟動 Frontend
cd frontend
npm install
npm run dev
