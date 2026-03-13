# garage_backend

车辆管理系统后端（Spring Boot 2.7 + MyBatis-Plus + MySQL + Spring Security + JWT + WebSocket）。

## 1. 技术栈
- Java 8
- Spring Boot 2.7.6
- MyBatis-Plus 3.5.1
- Spring Security（JWT 鉴权）
- WebSocket（实时监控推送）
- MySQL 8

## 2. 运行前准备
1. 确保 MySQL 可用，并创建/使用数据库 `garage_db`。
2. 修改 `src/main/resources/application.yml` 中数据库账号密码（默认 `root/123456`）。
3. 执行初始化脚本或迁移脚本：
   - 全量初始化：`mysql_init.sql`
   - 增量迁移（已有库升级）：`migration_v2.sql`

## 3. 启动
```bash
mvn -DskipTests clean package
java -jar target/garage_backend-0.0.1-SNAPSHOT.jar
```

默认地址：`http://localhost:9999/garage`

## 4. 默认账号
- 管理员：`admin / 123456`
- 普通用户：`user1 / 123456`

说明：首次登录旧明文账号后会自动升级为 BCrypt 密码。

## 5. 已实现核心能力
- 用户注册/登录/注销
- 基于角色的权限控制（admin/user）
- 未登录拦截、越权校验
- 车辆管理、车位管理、驾驶档案管理
- 停车预约（创建/取消，转入库仅管理员）
- 停车入库/出库（时长与费用计算）
- 我的车位聚合查询
- 个人中心聚合数据
- 实时监控 REST + WebSocket 推送
- 统一响应结构与全局异常处理

## 6. 可扩展架构（答辩可讲）
为兼容“车牌识别、移动支付、后续微服务拆分”，项目新增了可插拔集成层与支付编排层：

- `PaymentGateway` 支付网关接口（端口）：当前内置 `MockPaymentGateway`，后续接入微信/支付宝只需新增实现类。
- `CheckoutPaymentService` 出库支付编排：负责“创建支付单 -> 回调标记已支付 -> 完成出库”的标准流程。
- `PlateRecognitionService` 车牌识别接入服务：接收识别事件并给出标准化决策（入库建议/支付建议）。
- `OpenIntegrationController` 开放集成接口：通过 `X-Integration-Key` 进行系统间调用鉴权，避免与用户 JWT 鉴权耦合。

这套设计可平滑演进到微服务：
- `parking-core`（车位/在库）
- `reservation-service`（预约）
- `payment-service`（支付单、回调、对账）
- `edge-integration-service`（车牌识别设备、支付回调聚合）

当前单体中已按“端口 + 编排”方式组织，拆分时接口契约可复用。

## 7. 新增接口（扩展能力）
- 用户侧（JWT）：
  - `POST /payment/createCheckoutOrder` 创建出库支付单
  - `GET /payment/queryCheckoutOrder` 查询支付单
  - `POST /payment/completeCheckout` 支付完成后执行出库
- 开放集成侧（`X-Integration-Key`）：
  - `POST /open/integration/plate/analyze` 车牌识别事件分析
  - `POST /open/integration/payment/mock/paid` 模拟支付回调

可通过 `application.yml` 调整：
- `integration.access-key`
- `integration.payment.provider`
- `integration.payment.order-expire-minutes`

## 8. WebSocket
- 连接地址：`ws://localhost:9999/garage/ws/realtime?token=JWT`
- 仅管理员可连接
- 推送频率：默认 5 秒（`monitor.push-interval-ms`）

## 9. 接口文档
详见：[API_DOC.md](./API_DOC.md)

## 10. 测试清单
详见：[TEST_CHECKLIST.md](./TEST_CHECKLIST.md)
