# 校园二手交易平台

基于 **Java Spring Boot + Redis + RabbitMQ + MySQL** 的校园二手交易平台，融入 **AI Agent**（智能推荐 + 议价咨询）。业务原创（校园二手交易），技术架构参考黑马点评（Redis 高并发实战）。

> 软件工程课程设计 · 3 人团队 · 南京航空航天大学计算机学院

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Java 17 · Spring Boot 3.2 · MyBatis-Plus 3.5 · Redis · RabbitMQ |
| 前端 | Vue3 · Element Plus |
| 数据库 | MySQL 8.0 |
| AI Agent | DeepSeek（chat-completions API） |

## 环境要求

- JDK 17、Maven 3.8+、Node 18+、MySQL 8.0
- Redis 5+（本项目用 `tools/redis/redis-server.exe`，端口 6379）
- RabbitMQ 3.13（端口 5672，管理界面 15672，账号 guest/guest）

## 项目结构

```
nuaa-secondhand/
├── backend/    # Spring Boot 后端
│   └── src/main/java/com/nuaa/secondhand/
│       ├── controller/  # 接口层
│       ├── service/     # 业务层（缓存/秒杀/Feed/Agent）
│       ├── mapper/      # MyBatis-Plus
│       ├── entity/      # 实体
│       └── config/      # Redis/RabbitMQ/MyBatis-Plus 配置
├── frontend/   # Vue3 前端
├── sql/        # 建库脚本
├── tools/      # 本地 Redis（不提交）
└── docs/       # 需求/设计/测试文档
```

## 快速启动

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

### 2. 配置密钥

复制一份本地配置（已被 `.gitignore` 忽略，不会提交）：

```bash
cd backend/src/main/resources
cp application.yml application-local.yml
```

编辑 `application-local.yml`，填入真实的 MySQL 密码和 DeepSeek Key：

```yaml
spring:
  datasource:
    password: 你的MySQL密码
deepseek:
  api-key: sk-你的DeepSeekKey
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173（前端）、http://localhost:8081（后端）。

## 功能模块

| 模块 | 说明 | 关键技术 |
|---|---|---|
| 登录 | 学号 + 验证码登录，自动注册 | Redis 存 token |
| 商品 | 发布/详情/搜索/附近 | 缓存穿透·击穿·雪崩、GeoHash |
| 抢购 | 热门商品秒杀 | Lua 原子扣减 + RabbitMQ 异步下单 |
| 订单 | 下单/状态流转 | 已拍→面交中→已完成→已取消 |
| 社交 | 关注/粉丝/共同关注 | Set 交集 |
| Feed 流 | 卖家上新动态推送 | 推模式 ZSet |
| 签到 | 每日签到 + 连续天数 | BitMap + BitField |
| UV | 商品浏览量统计 | HyperLogLog |
| AI Agent | 智能推荐 + 议价咨询 | DeepSeek |

## 技术亮点

1. **缓存三大问题**：穿透（空值缓存）、击穿（互斥锁重建）、雪崩（TTL 随机）
2. **秒杀并发安全**：Lua 脚本原子判断库存+去重+扣减，RabbitMQ 异步削峰
3. **分布式锁**：Redis setnx + UUID 标识 + Lua 校验释放
4. **Feed 流**：推模式，粉丝收件箱 ZSet
5. **AI Agent**：智能推荐 + 议价咨询，失败降级

## 项目文档

| 文档 | 说明 |
|---|---|
| [环境配置手册](docs/00-环境配置手册.md) | 从零跑通 demo（其他电脑快速上手） |
| [需求规格说明书](docs/01-需求规格说明书.md) | 功能/非功能需求、用例 |
| [概要设计说明书](docs/02-概要设计说明书.md) | 架构、技术选型、模块划分 |
| [详细设计说明书](docs/03-详细设计说明书.md) | 接口、时序、核心类 |
| [数据库设计](docs/04-数据库设计.md) | 表结构、ER 图、Redis 数据结构 |
| [测试报告](docs/05-测试报告.md) | 功能用例 + 并发测试 |
| [部署说明](docs/06-部署说明.md) | 打包、生产部署、演示脚本 |
