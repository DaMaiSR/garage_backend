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
- 停车预约（创建/取消/转入库）
- 停车入库/出库（时长与费用计算）
- 我的车位聚合查询
- 个人中心聚合数据
- 实时监控 REST + WebSocket 推送
- 统一响应结构与全局异常处理

## 6. WebSocket
- 连接地址：`ws://localhost:9999/garage/ws/realtime?token=JWT`
- 仅管理员可连接
- 推送频率：默认 5 秒（`monitor.push-interval-ms`）

## 7. 接口文档
详见：[API_DOC.md](./API_DOC.md)

## 8. 测试清单
详见：[TEST_CHECKLIST.md](./TEST_CHECKLIST.md)
