# 数据库文档

## 概述

本文档提供了 Easy Recon SDK 的数据库相关文档，包括数据库设计、表结构、索引优化、迁移策略等内容。了解这些信息有助于开发者更好地理解 SDK 的数据存储机制，优化数据库性能，以及进行自定义扩展。

## 数据库设计

### 设计原则

- **范式设计**：遵循数据库设计范式，减少数据冗余
- **性能优先**：为常用查询创建合适的索引
- **可扩展性**：支持水平扩展和分库分表
- **数据完整性**：使用约束和事务保证数据完整性
- **兼容性**：支持 MySQL 和 PostgreSQL 等主流关系型数据库

### 表结构

#### 1. 对账订单主表 (`recon_order_main`)

| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| `order_no` | `VARCHAR(64)` | `PRIMARY KEY` | 订单号 |
| `merchant_id` | `VARCHAR(32)` | `NOT NULL` | 商户ID |
| `merchant_name` | `VARCHAR(128)` | `NOT NULL` | 商户名称 |
| `order_amount` | `DECIMAL(16,2)` | `NOT NULL` | 订单金额 |
| `actual_amount` | `DECIMAL(16,2)` | `NOT NULL` | 实际金额 |
| `recon_status` | `TINYINT` | `NOT NULL DEFAULT 0` | 对账状态：0-待对账，1-对账成功，2-对账失败，3-部分成功 |
| `order_time` | `DATETIME` | `NOT NULL` | 订单时间 |
| `pay_time` | `DATETIME` | `NOT NULL` | 支付时间 |
| `create_time` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

**索引**：
- 主键索引：`PRIMARY KEY (order_no)`
- 联合索引：`INDEX idx_merchant_status (merchant_id, recon_status)`
- 联合索引：`INDEX idx_time_range (order_time, pay_time)`

#### 2. 对账订单分账子表 (`recon_order_split_sub`)

| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | 自增ID |
| `order_no` | `VARCHAR(64)` | `NOT NULL` | 订单号 |
| `sub_order_no` | `VARCHAR(64)` | `NOT NULL` | 子订单号 |
| `merchant_id` | `VARCHAR(32)` | `NOT NULL` | 商户ID |
| `split_amount` | `DECIMAL(16,2)` | `NOT NULL` | 分账金额 |
| `status` | `TINYINT` | `NOT NULL DEFAULT 0` | 状态：0-待处理，1-处理成功，2-处理失败 |
| `create_time` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

**索引**：
- 主键索引：`PRIMARY KEY (id)`
- 联合索引：`INDEX idx_order_suborder (order_no, sub_order_no)`
- 联合索引：`INDEX idx_merchant_status (merchant_id, status)`

#### 3. 对账异常表 (`recon_exception`)

| 字段名 | 数据类型 | 约束 | 描述 |
|-------|---------|------|------|
| `id` | `BIGINT` | `PRIMARY KEY AUTO_INCREMENT` | 自增ID |
| `order_no` | `VARCHAR(64)` | `NOT NULL` | 订单号 |
| `merchant_id` | `VARCHAR(32)` | `NOT NULL` | 商户ID |
| `exception_type` | `TINYINT` | `NOT NULL` | 异常类型：1-金额不匹配，2-订单不存在，3-数据格式错误，4-其他异常 |
| `exception_msg` | `VARCHAR(512)` | `NOT NULL` | 异常信息 |
| `order_amount` | `DECIMAL(16,2)` | `NULL` | 订单金额 |
| `actual_amount` | `DECIMAL(16,2)` | `NULL` | 实际金额 |
| `alarm_status` | `TINYINT` | `NOT NULL DEFAULT 0` | 告警状态：0-未告警，1-已告警 |
| `create_time` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | 创建时间 |
| `update_time` | `DATETIME` | `NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | 更新时间 |

**索引**：
- 主键索引：`PRIMARY KEY (id)`
- 联合索引：`INDEX idx_order_merchant (order_no, merchant_id)`
- 联合索引：`INDEX idx_exception_alarm (exception_type, alarm_status)`
- 联合索引：`INDEX idx_create_time (create_time)`

### 表关系

- **recon_order_main** 与 **recon_order_split_sub**：一对多关系，通过 `order_no` 关联
- **recon_order_main** 与 **recon_exception**：一对多关系，通过 `order_no` 关联

## 数据库兼容性

### 支持的数据库

- **MySQL**：5.7+ 
- **PostgreSQL**：10+ 

### 方言适配

SDK 使用数据库方言抽象层，自动适配不同数据库的语法差异：

- **MySQL**：使用 MySQL 特定语法
- **PostgreSQL**：使用 PostgreSQL 特定语法
- **其他数据库**：可通过扩展方言实现支持

### 数据类型映射

| 通用类型 | MySQL | PostgreSQL |
|---------|-------|------------|
| 字符串 | `VARCHAR` | `VARCHAR` |
| 整数 | `INT`, `BIGINT` | `INTEGER`, `BIGINT` |
| 小数 | `DECIMAL` | `DECIMAL` |
| 时间 | `DATETIME` | `TIMESTAMP` |
| 布尔值 | `TINYINT` | `BOOLEAN` |

## 数据迁移

### 迁移工具

SDK 使用 Flyway 进行数据库迁移，确保数据库结构的一致性和版本控制。

### 迁移脚本

迁移脚本存放在 `resources/db/migration` 目录下，命名格式为 `V{版本号}__{描述}.sql`。

#### 示例迁移脚本

```sql
-- V1__create_recon_tables.sql

-- 创建对账订单主表
CREATE TABLE IF NOT EXISTS recon_order_main (
    order_no VARCHAR(64) PRIMARY KEY,
    merchant_id VARCHAR(32) NOT NULL,
    merchant_name VARCHAR(128) NOT NULL,
    order_amount DECIMAL(16,2) NOT NULL,
    actual_amount DECIMAL(16,2) NOT NULL,
    recon_status TINYINT NOT NULL DEFAULT 0,
    order_time DATETIME NOT NULL,
    pay_time DATETIME NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_merchant_status ON recon_order_main(merchant_id, recon_status);
CREATE INDEX idx_time_range ON recon_order_main(order_time, pay_time);

-- 创建对账订单分账子表
CREATE TABLE IF NOT EXISTS recon_order_split_sub (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    sub_order_no VARCHAR(64) NOT NULL,
    merchant_id VARCHAR(32) NOT NULL,
    split_amount DECIMAL(16,2) NOT NULL,
    status TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_order_suborder ON recon_order_split_sub(order_no, sub_order_no);
CREATE INDEX idx_merchant_status ON recon_order_split_sub(merchant_id, status);

-- 创建对账异常表
CREATE TABLE IF NOT EXISTS recon_exception (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    merchant_id VARCHAR(32) NOT NULL,
    exception_type TINYINT NOT NULL,
    exception_msg VARCHAR(512) NOT NULL,
    order_amount DECIMAL(16,2) NULL,
    actual_amount DECIMAL(16,2) NULL,
    alarm_status TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX idx_order_merchant ON recon_exception(order_no, merchant_id);
CREATE INDEX idx_exception_alarm ON recon_exception(exception_type, alarm_status);
CREATE INDEX idx_create_time ON recon_exception(create_time);
```

### 迁移流程

1. **初始化**：首次启动时，SDK 会自动执行迁移脚本，创建所需的表结构
2. **版本控制**：每次更新时，SDK 会执行新的迁移脚本，保持数据库结构与代码版本一致
3. **回滚**：支持迁移回滚，可回退到之前的版本

## 性能优化

### 索引优化

- **查询分析**：使用 `EXPLAIN` 分析查询执行计划
- **索引覆盖**：为常用查询创建覆盖索引
- **复合索引**：合理设计复合索引，考虑查询顺序
- **索引维护**：定期分析和优化索引，删除无用索引

### 查询优化

- **避免全表扫描**：使用索引加速查询
- **分页查询**：使用高效的分页查询方式
- **批量操作**：对批量操作使用批量 SQL 语句
- **减少 JOIN**：避免过多 JOIN 操作，考虑使用子查询

### 连接池优化

- **连接池配置**：根据系统负载配置合理的连接池大小
- **连接超时**：设置合理的连接超时和查询超时
- **连接验证**：定期验证连接有效性，避免使用失效连接

### 数据分区

- **水平分区**：对大表进行水平分区，按时间或商户ID分区
- **分区策略**：根据查询模式选择合适的分区策略
- **分区维护**：定期维护分区，清理历史数据

## 数据安全

### 访问控制

- **最小权限**：数据库用户只授予必要的权限
- **只读用户**：为报表和查询创建只读用户
- **权限分离**：分离读写权限，避免越权操作

### 数据加密

- **敏感字段**：对敏感字段（如商户信息）进行加密存储
- **传输加密**：使用 SSL/TLS 加密数据库连接
- **备份加密**：对数据库备份进行加密

### 审计日志

- **操作审计**：记录对敏感数据的操作
- **登录审计**：记录数据库登录事件
- **异常检测**：检测异常访问模式

## 数据维护

### 定期维护

- **统计分析**：定期运行 `ANALYZE TABLE` 更新统计信息
- **碎片整理**：定期运行 `OPTIMIZE TABLE` 整理碎片
- **索引重建**：定期重建索引，提高查询性能

### 数据清理

- **过期数据**：定期清理过期数据，归档历史数据
- **异常数据**：定期清理无效的异常记录
- **备份策略**：制定合理的备份策略，包括全量备份和增量备份

### 监控与告警

- **性能监控**：监控数据库性能指标，如 QPS、响应时间、连接数
- **空间监控**：监控数据库空间使用情况
- **错误监控**：监控数据库错误日志
- **告警阈值**：设置合理的告警阈值，及时发现异常

## 最佳实践

### 开发环境

- **使用 Docker**：使用 Docker 容器化数据库，确保环境一致性
- **自动迁移**：启用自动数据库迁移，简化开发流程
- **测试数据**：使用测试数据填充数据库，便于功能测试

### 测试环境

- **与生产环境一致**：测试环境配置与生产环境保持一致
- **性能测试**：在测试环境进行性能测试，模拟生产负载
- **故障演练**：定期进行故障演练，测试系统恢复能力

### 生产环境

- **高可用**：使用主从复制或集群，确保高可用性
- **读写分离**：实施读写分离，提高系统吞吐量
- **灾备方案**：制定详细的灾备方案，包括数据备份和恢复流程
- **监控系统**：部署专业的数据库监控系统，及时发现问题

## 常见问题与解决方案

### 1. 数据库连接失败

**原因**：
- 数据库服务未启动
- 连接信息配置错误
- 网络连接问题
- 防火墙阻止连接

**解决方案**：
- 检查数据库服务状态
- 验证连接信息配置
- 检查网络连接
- 配置防火墙规则
- 增加连接超时设置

### 2. 表结构创建失败

**原因**：
- 数据库用户权限不足
- 迁移脚本语法错误
- 数据库版本不兼容

**解决方案**：
- 确保数据库用户有创建表的权限
- 检查迁移脚本语法
- 验证数据库版本兼容性
- 查看详细的错误日志

### 3. 查询性能慢

**原因**：
- 缺少必要的索引
- 查询语句不合理
- 数据库负载过高
- 表数据量过大

**解决方案**：
- 添加合适的索引
- 优化查询语句
- 调整数据库参数
- 考虑分表或分区

### 4. 数据一致性问题

**原因**：
- 并发操作导致数据冲突
- 事务处理不当
- 网络中断导致部分操作失败

**解决方案**：
- 使用事务保证数据一致性
- 合理设计并发控制
- 实现幂等操作
- 定期校验数据一致性

### 5. 存储空间不足

**原因**：
- 数据量增长过快
- 日志文件过大
- 备份占用空间

**解决方案**：
- 清理过期数据
- 配置合理的日志轮转
- 优化备份策略
- 考虑扩容存储

## 扩展与定制

### 自定义表结构

如果需要自定义表结构，可以：

1. **扩展现有表**：通过迁移脚本添加字段
2. **创建新表**：创建新的业务表，与现有表关联
3. **修改索引**：根据业务需求调整索引

### 自定义数据类型

可以根据业务需求扩展数据类型：

1. **枚举类型**：使用枚举类型表示状态
2. **JSON 类型**：使用 JSON 类型存储复杂数据
3. **空间类型**：使用空间类型存储地理位置数据

### 自定义迁移脚本

可以创建自定义迁移脚本：

1. **数据初始化**：初始化基础数据
2. **数据转换**：转换历史数据格式
3. **结构调整**：调整表结构以适应新需求

## 总结

本文档提供了 Easy Recon SDK 的数据库设计和最佳实践指南。遵循这些指南可以确保数据库的性能、可靠性和安全性，为对账系统提供坚实的数据基础。

数据库设计是一个持续优化的过程，需要根据业务需求和系统负载不断调整和改进。通过合理的设计和维护，可以构建一个高效、可靠的对账数据存储系统。