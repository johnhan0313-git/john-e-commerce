# John E-Commerce 项目记忆

> 本文件记录仓库中可复用、相对稳定的事实和协作约定。开始任务前先阅读；实现发生结构性变化后同步更新。具体代码与配置始终是最终事实来源。

## 项目定位

- 多租户、多门户电商系统，目标是通过模块开关支持 B2C、B2B、B2B2C、团购、点餐、分销等业态。
- 当前形态是一个 Spring Boot 模块化单体后端，搭配三个独立 Vue SPA：运营管理端、消费者商城、商家工作台。
- 主要业务域：租户与模块、用户身份、商户/店铺、商品、购物车/交易、营销、支付/账本/结算/跨境、采购/库存/物流、内容和统计。

## 仓库地图

- `backend/`：Java 17、Spring Boot 3.3.6、Spring Security、MyBatis-Plus、PostgreSQL、Redis、MinIO。
- `backend/src/main/java/com/john/ecommerce/module/`：按业务域组织的 controller/service/mapper/entity/dto。
- `backend/src/main/java/com/john/ecommerce/common/`：统一响应、异常、上下文、枚举、模块门控和实体基类。
- `apps/admin/`：Vue 3 + TypeScript + Vite + Pinia + Element Plus，端口 `3021`；租户运营后台。
- `apps/mall/`：Vue 3 + TypeScript + Vite + Pinia，自有 CSS，端口 `3022`；消费者商城。
- `apps/merchant/`：Vue 3 + TypeScript + Vite + Pinia + Element Plus，端口 `3023`；商户入驻、商品和订单工作台。
- `scripts/sql/`：手工维护的 PostgreSQL 版本化 SQL，必须按版本号顺序执行；项目未集成 Flyway/Liquibase。
- `run.sh`：macOS 本地一键启停；通过 `john-server` SSH 隧道连接 PostgreSQL、Redis、MinIO。
- `docker-compose.yml`：开发镜像组合，但依赖宿主机映射端口上的基础设施。
- `docker-compose.prod.yml`、`deploy/`、`scripts/init-john-server.sh`：服务器部署资料。
- 生产 nginx 必须同时声明 `mall.cool-app.me`、`eadmin.cool-app.me`、`merchant.cool-app.me` 三个 `server_name`，分别反代到对应容器；新增域名后要同步 nginx 配置并 reload。

## 关键架构事实

### 请求、鉴权与租户

- 后端统一 context path 为 `/api`，端口 `8020`；Swagger 为 `/api/swagger-ui.html`。
- 前端 axios 均使用相对地址 `/api`，开发时由 Vite 代理，部署时由 nginx 同源反代。
- 登录采用邮箱验证码。`portal` 为 `mall`、`merchant` 或 `admin`：商城首次登录自动获得 buyer 身份；商家端还会获得 seller；管理端不自动注册且必须已有 ops 身份。
- 本地默认固定验证码 `123456`；生产必须将 `AUTH_FIXED_CODE` 置空并配置 SMTP。
- JWT 携带 `userId`、`tenantId`、`userType`、`identities`，默认有效期 7 天。`JWT_SECRET` 至少 32 字节。
- JWT 过滤器把租户和用户写入线程上下文，并在请求结束时清理。不要绕过上下文清理。
- 三个前端都会发送 `X-Tenant-Id`（默认 `1`，可由 `VITE_TENANT_ID` 覆盖）；商家端另发 `X-Shop-Id`。
- `/auth/**`、`/public/**`、文档和物流 webhook 匿名可访问；公开业务接口仍需用请求头或参数明确租户。
- MyBatis-Plus 的租户行拦截器自动追加 `tenant_id`。仅 `t_tenant`、`t_tenant_config`、`t_system_config`、`t_module_def` 被排除。新增共享表时必须审视排除列表；无租户上下文时普通租户表查询会按 `tenant_id IS NULL` 处理。

### 模块门控与权限

- `@RequiresModule` + `FeatureGateInterceptor` 实现租户模块开关；模块码集中在 `common/module/ModuleCodes.java`。
- 基础模块为 tenant、product、trade；其余包括 activity、payment、ledger、settle、fulfillment、purchase、merchant、content、statistics、crossborder。
- 前端路由守卫只负责体验，后端注解才是模块边界。
- Spring Security 已启用方法安全，但当前角色身份主要来自 JWT identities。新增敏感管理接口时，不要只依赖“已登录”或前端隐藏，需核对/补充服务端角色授权。
- 已落地的服务端边界：商户/店铺平台审核与列表要求 `ops`；商家自助入口要求 `seller`；租户创建、品牌修改、模块开关要求 `ops`；订单读取/取消在服务层校验买家归属，店铺门户使用店铺归属读取入口；已登录 JWT 与 `X-Tenant-Id` 冲突会被拒绝。
- 支付账户/渠道路由、分账、结算、平台账本、采购和库存管理接口要求 `ops`；消费者保留收银台、本人支付和本人账本能力。手工余额充值属于运营动作，普通用户不可调用。

### 商品、店铺与交易主链路

- 商品模型以 SPU/SKU 为核心；SPU 可归属 merchant/shop。商家端通过当前店铺上下文管理商品和订单。
- 下单时 `OrderSplitter` 先按 shop、再按 warehouse 拆单；warehouse 当前固定为 `0`，因此实质按店铺拆单，并形成 order group。
- 订单状态码：0 待支付、1 已支付、2 已发货、3 已送达、4 已完成、5 已取消、6 退款中、7 已退款、8 部分发货。
- 合法状态迁移集中在 `trade/service/statemachine/OrderStateMachine.java`，修改订单流程时必须同步状态机和测试，不能在 controller/service 任意写状态。
- 待支付订单有 `payDeadline`；后台任务按配置自动取消超时单。支付超时默认 30 分钟，扫描/延迟默认 60 秒。
- 支付层有渠道路由、mock/balance 渠道、账本、分账、结算和跨境骨架；金额语义不统一：订单金额多为 `DECIMAL(12,2)`，账本/结算多为 `BIGINT` 分，跨层传递必须显式换算。
- 创建支付单时后端会校验订单属于当前用户、订单可支付、未超期，并要求客户端金额与订单剩余应付完全一致；重复订单支付项会被拒绝。mock 回调仅允许 MOCK 渠道和支付单所属买家调用，并通过条件更新保证重复/并发回调只执行一次下游副作用。
- 真实支付回调入口为 `POST /api/payment/callback?payNo=...`，匿名可调用但必须带 `X-Payment-Timestamp` 和 `X-Payment-Signature`；签名为 `HMAC-SHA256(payNo + "." + timestamp, PAYMENT_WEBHOOK_SECRET)` 的小写 hex，时间窗为 5 分钟。未配置 `PAYMENT_WEBHOOK_SECRET` 时回调拒绝。回调与 mock 共用幂等完成逻辑。
- 物流回调入口 `POST /api/logistics/webhook/{trackingNo}` 同样匿名但必须带 `X-Logistics-Timestamp` 和 `X-Logistics-Signature`；签名 payload 为 `trackingNo.status.eventTime.timestamp`，密钥来自 `LOGISTICS_WEBHOOK_SECRET`，时间窗 5 分钟。未配置密钥时拒绝。
- 商家结算只读接口为 `/shop/settlements/bills` 和 `/shop/settlements/orders`，通过当前 `X-Shop-Id` 和已认证 seller 强制限定店铺范围；平台结算接口仍仅限 ops。
- 退款入口为 `POST /api/refund/order/{orderId}`，仅订单本人 buyer 可申请；平台通过 `PUT /api/refund/{id}/audit?approved=true|false` 审核。申请会校验已支付订单和累计可退余额，审核通过后调用支付渠道退款；全额退款才推进订单到 `REFUNDED`，部分退款仍需后续补充逆向库存/结算冲正。
- 退款查询接口为 `GET /api/refund` 和 `GET /api/refund/{id}`；buyer 仅能看到自己的申请，ops 可查看全租户范围。退款状态：0 待审核、1 渠道处理中、2 已完成、3 已拒绝、4 渠道失败；只有待审核申请可审核，避免重复审核。
- 异步退款回调为 `POST /api/refund/callback?refundNo=...&success=true|false`，使用支付 webhook 的时间戳/签名请求头和 `PAYMENT_WEBHOOK_SECRET`；签名 payload 为 `refundNo.successFlag.timestamp`（successFlag 为 1/0）。终态重复回调直接返回当前结果。
- 退款完成会生成方向为 `REVERSE`、业务类型为 `REFUND` 的反向 settlement order，金额为退款金额的分值；使用 `idempotent_key=REFUND:{refundId}` 防止同步结果与异步回调重复冲正。反向单当前保持待归账，由 ops 账单流程处理，不会自动修改已结算账单。
- `V021` 为 `t_refund` 补充 `user_id` 并新增 `t_refund_item`。退款申请必须提交 `{amount, reason, items:[{orderItemId, quantity}]}`；服务端按订单项实付金额和数量计算明细金额，合计必须与申请金额一致，累计有效退款数量不能超过购买数量。
- 订单仍为 `PAID`（未发货）时，退款成功按明细恢复默认批次库存，并通过 `stock_restored` 防止重复入库。已发货/已送达订单退款不自动恢复库存，需等待退货收货流程。

## 数据库约定

- SQL 当前从 `V001` 延伸到 `V021`；以目录实际文件为准，按数字顺序全量执行。
- 后端不会自动迁移。改表时新增后续版本脚本，并考虑已部署数据库需要的增量执行方式；不要只改旧初始化脚本。
- 已预留 `SPRING_FLYWAY_ENABLED` 和 `spring.flyway.locations=classpath:db/migration` 配置，但当前未引入 Flyway 依赖，默认关闭；现有 `scripts/sql` 仍是唯一实际迁移来源。引入前需准备可用 Maven 仓库，并处理现有环境基线，不能直接打开开关。
- 主键为应用生成的 BIGINT（MyBatis-Plus `assign_id`），不是自增列。
- 表名前缀 `t_`；逻辑删除字段 `delete_flag`（0 正常、1 删除）。
- 业务表通常含 `tenant_id` 及审计字段 `created_at`、`updated_at`、`created_by`、`updated_by`、`idempotent_key`。
- JSONB 依赖 datasource 的 `stringtype=unspecified` 配置；修改 JSON 字段映射时留意 TypeHandler。

## API 与前端约定

- 普通后端响应使用 `R<T>`：`{ code, message, data }`，成功业务码为 `200`；分页通常使用 MyBatis-Plus `Page` 或项目 `PageResult`。
- mall 客户端会检查 HTTP 200 内的业务码；admin/merchant 当前主要直接返回响应体。改统一错误处理时需同时检查三个 client。
- token key 分别是 `admin_token`、`token`（mall）、`merchant_token`；商家当前店铺 key 为 `merchant_active_shop_id`。
- 三个前端是独立工程，各自有 `package-lock.json`，没有根级 npm workspace。依赖和命令需在对应 `apps/*` 目录运行。
- admin/merchant 共享许多交互模式但没有共享组件包；修复一端的上传、富文本、鉴权或品牌样式问题时，检查另一端是否也需一致修复。

## 开发与验证

- 本地全量启动：`./run.sh start`；状态：`./run.sh status`；分服务：`./run.sh backend|admin|mall|merchant`；停止：`./run.sh stop`。
- `run.sh` 要求 Java 17、Maven、Node/npm，以及可用的 `john-server` SSH/Tailscale 链路；日志和 pid 在被忽略的 `.run/`。
- 后端构建：在 `backend/` 运行 `mvn test` 或 `mvn package`。
- 集成测试优先用 Testcontainers 的 PostgreSQL 16 + Redis 7；Docker 不可用时退回 `application-test.yml` 指定的本地隧道服务。`mvn test` 配置会同时包含 `*Test` 和 `*IT`。
- 前端构建：分别在三个 app 目录运行 `npm run build`。merchant 另有 `npm test`；admin 和 mall 当前没有测试脚本。
- 后端重要测试覆盖邮箱登录/身份、订单状态机与拆单、营销叠加、FEFO、支付路由，以及登录、金钱链路、履约/模块门控、商户门户集成路径。
- 验证应按改动范围最小化执行；涉及 DTO/API 契约时至少构建受影响前端和后端，涉及 SQL/租户/交易/支付时优先跑相关集成测试。
- 当前仍未完成：Flyway 迁移、统一分为金额、真实支付渠道签名 webhook/主动查单、完整权限矩阵（内容/商品运营/跨境等域）、商家只读结算视图和售后闭环；不要把当前权限收口误认为全量安全审计已结束。

## 部署与配置

- 生产组合依赖外部 Docker 网络：`john-postgresql_default`、`john-redis_default`、`john-minio_default`、`john-nginx_default`。
- 生产公开域名在 compose/nginx 配置中体现：mall、eadmin、merchant；修改域名时同步 CORS 和 nginx。
- `.env*` 默认忽略，仅 `.env.example` 可提交。不要提交 SMTP 密码、真实 JWT、SSH 信息或其他凭据。
- compose 中存在开发/占位默认凭据，不能视为安全的生产密钥。

## 协作护栏与易踩坑

- 工作树可能已有用户改动；开始前执行 `git status --short`，不要覆盖或回滚无关修改。
- 当前没有自动数据库迁移器；仅改实体不会改变数据库，仅改 SQL 也不会自动应用到运行环境。
- 多租户查询/写入应依赖正确的 `TenantContext`，跨租户管理逻辑必须显式保存并恢复原上下文。
- 新公共接口应放在 `/public/**` 或同步更新安全配置，并自行建立租户上下文；切勿让公开查询无租户边界。
- 新模块能力应同时考虑：模块定义/seed、后端 `@RequiresModule`、管理端路由/菜单，以及集成测试。
- 新状态或支付/库存副作用要考虑事务、幂等、重复回调和失败补偿，优先复用已有状态机、路由器与服务边界。
- 不要把 `.run/`、`target/`、`node_modules/`、`dist/` 当作源码或提交内容。
