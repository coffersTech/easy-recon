# API 参考文档

## 概述

本文档提供了 Easy Recon SDK 各语言版本的详细 API 参考。SDK 支持以下语言版本：

- [Java Spring Boot Starter](#java-spring-boot-starter)
- [Go SDK](#go-sdk)
- [Python SDK](#python-sdk)
- [Node.js SDK](#nodejs-sdk)

## Java Spring Boot Starter

**描述**：基于 Spring Boot 的 Java 版本 SDK，提供自动配置和集成支持。

**核心特性**：
- 自动配置和启动
- 异步对账支持
- 线程池优化
- 配置加密
- Flyway 数据库迁移

**文档链接**：[java-spring-boot-starter.md](java-spring-boot-starter.md)

## Go SDK

**描述**：基于 Go 语言的 SDK 版本，提供简洁高效的实现。

**核心特性**：
- 并发对账支持
- 标准 Go 项目结构
- 接口化设计
- 轻量级实现

**文档链接**：[go-sdk.md](go-sdk.md)

## Python SDK

**描述**：基于 Python 语言的 SDK 版本，提供简洁易用的 API。

**核心特性**：
- 异步处理支持
- 数据类实体
- 简洁的 API 设计
- 易于集成

**文档链接**：[python-sdk.md](python-sdk.md)

## Node.js SDK

**描述**：基于 Node.js 语言的 SDK 版本，提供异步非阻塞的实现。

**核心特性**：
- Promise 异步支持
- 批量处理能力
- 事件驱动设计
- 易于集成到 Node.js 应用

**文档链接**：[nodejs-sdk.md](nodejs-sdk.md)

## 通用概念

### 核心流程

1. **初始化**：创建数据库连接、存储库、告警服务等组件
2. **实时对账**：处理实时交易对账
3. **定时核账**：处理批量定时对账
4. **异常处理**：处理对账过程中的异常
5. **告警通知**：发送异常告警通知

### 数据模型

- **ReconOrderMain**：对账订单主记录
- **ReconOrderSplitSub**：对账订单分账子记录
- **ReconException**：对账异常记录
- **ReconNotifyLog**：对账通知日志
- **ReconRule**：对账规则

### 服务组件

- **EasyReconTemplate**：核心模板类，提供主要对账方法
- **RealtimeReconService**：实时对账服务
- **TimingReconService**：定时核账服务
- **AlarmService**：告警服务
- **SQLReconRepository**：SQL 存储库

### 配置项

- **数据库配置**：数据库连接信息
- **线程池配置**：线程池大小、队列容量等
- **告警配置**：告警类型、Webhook URL 等
- **对账配置**：金额容差、批处理大小等

## 版本历史

| 版本号 | 发布日期 | 主要变更 |
|--------|----------|----------|
| 1.0.0 | 2026-02-08 | 初始版本，支持 Java、Go、Python、Node.js 语言 |

## 联系信息

如有问题或建议，欢迎联系我们：

- Email: ryan@example.com
- GitHub: https://github.com/example/easy-recon-sdk
