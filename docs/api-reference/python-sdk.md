# Python SDK API 参考

## 1. 核心模块

### 1.1 core.easy_recon_template

**描述**：SDK 的核心模板类，提供对账相关的核心方法。

#### 1.1.1 EasyReconTemplate

**描述**：SDK 的核心模板类，提供对账相关的核心方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `__init__(self, realtime_recon_service, timing_recon_service)` | realtime_recon_service: 实时对账服务<br>timing_recon_service: 定时核账服务 | - | 初始化模板实例 |
| `do_realtime_recon(self, order_main, split_subs)` | order_main: 订单主记录<br>split_subs: 分账子记录列表 | `bool`：对账结果 | 执行实时对账 |
| `do_timing_recon(self, date_str)` | date_str: 对账日期（yyyy-MM-dd） | `bool`：对账结果 | 执行定时对账 |

**使用示例**：

```python
from core.easy_recon_template import EasyReconTemplate
from service.realtime_recon_service import RealtimeReconService
from service.timing_recon_service import TimingReconService
from service.alarm_service import AlarmService, LogAlarmStrategy
from repository.sql_recon_repository import SQLReconRepository
from dialect.recon_database_dialect import create_dialect
from entity.recon_order_main import ReconOrderMain
from entity.recon_order_split_sub import ReconOrderSplitSub
import mysql.connector
from datetime import datetime

# 初始化数据库连接
connection = mysql.connector.connect(
    host="localhost",
    user="root",
    password="password",
    database="easy_recon"
)

# 创建数据库方言
dialect = create_dialect(connection)

# 创建存储库
repo = SQLReconRepository(connection, dialect)

# 创建告警服务
alarm_service = AlarmService(LogAlarmStrategy())

# 创建对账服务
realtime_recon_service = RealtimeReconService(repo, alarm_service)
timing_recon_service = TimingReconService(repo, alarm_service)

# 创建模板
template = EasyReconTemplate(realtime_recon_service, timing_recon_service)

# 执行实时对账
order_main = ReconOrderMain(
    order_no="ORDER_123",
    merchant_id="MERCHANT_001",
    merchant_name="测试商户",
    order_amount=100.00,
    actual_amount=100.00,
    recon_status=0,
    order_time=datetime.now(),
    pay_time=datetime.now(),
    create_time=datetime.now(),
    update_time=datetime.now()
)

split_subs = [
    ReconOrderSplitSub(
        order_no="ORDER_123",
        sub_order_no="SUB_123",
        merchant_id="MERCHANT_001",
        split_amount=80.00,
        status=0,
        create_time=datetime.now(),
        update_time=datetime.now()
    )
]

result = template.do_realtime_recon(order_main, split_subs)
print(f"实时对账结果: {result}")

# 执行定时对账
from datetime import date, timedelta
date_str = (date.today() - timedelta(days=1)).strftime("%Y-%m-%d")
result = template.do_timing_recon(date_str)
print(f"定时对账结果: {result}")

# 关闭连接
connection.close()
```

### 1.2 service.realtime_recon_service

**描述**：实时对账服务，处理实时对账逻辑。

#### 1.2.1 RealtimeReconService

**描述**：实时对账服务，处理实时对账逻辑。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `__init__(self, repo, alarm_service)` | repo: 存储库<br>alarm_service: 告警服务 | - | 初始化服务实例 |
| `reconcile(self, order_main, split_subs)` | order_main: 订单主记录<br>split_subs: 分账子记录列表 | `bool`：对账结果 | 执行实时对账逻辑 |

### 1.3 service.timing_recon_service

**描述**：定时核账服务，处理定时对账逻辑。

#### 1.3.1 TimingReconService

**描述**：定时核账服务，处理定时对账逻辑。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `__init__(self, repo, alarm_service)` | repo: 存储库<br>alarm_service: 告警服务 | - | 初始化服务实例 |
| `reconcile(self, date_str)` | date_str: 对账日期（yyyy-MM-dd） | `bool`：对账结果 | 执行定时对账逻辑 |

### 1.4 service.alarm_service

**描述**：告警服务，处理对账过程中的异常告警。

#### 1.4.1 AlarmService

**描述**：告警服务，处理对账过程中的异常告警。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `__init__(self, alarm_strategy)` | alarm_strategy: 告警策略 | - | 初始化服务实例 |
| `alarm(self, exception)` | exception: 异常记录 | `None` | 发送告警 |

#### 1.4.2 LogAlarmStrategy

**描述**：日志告警策略，将异常信息记录到日志。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `alarm(self, exception)` | exception: 异常记录 | `None` | 发送日志告警 |

#### 1.4.3 DingTalkAlarmStrategy

**描述**：钉钉告警策略，通过钉钉机器人发送告警消息。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `__init__(self, webhook_url)` | webhook_url: 钉钉机器人 Webhook URL | - | 初始化策略实例 |
| `alarm(self, exception)` | exception: 异常记录 | `None` | 发送钉钉告警 |

### 1.5 repository.sql_recon_repository

**描述**：SQL 实现的对账存储库，处理数据库操作。

#### 1.5.1 SQLReconRepository

**描述**：SQL 实现的对账存储库，处理数据库操作。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `__init__(self, connection, dialect)` | connection: 数据库连接<br>dialect: 数据库方言 | - | 初始化存储库实例 |
| `save_order_main(self, order_main)` | order_main: 订单主记录 | `bool`：保存结果 | 保存对账订单主记录 |
| `batch_save_order_split_sub(self, split_subs)` | split_subs: 分账子记录列表 | `bool`：保存结果 | 批量保存分账子记录 |
| `save_exception(self, exception)` | exception: 异常记录 | `bool`：保存结果 | 保存异常记录 |
| `batch_save_exception(self, exceptions)` | exceptions: 异常记录列表 | `bool`：保存结果 | 批量保存异常记录 |
| `get_order_main_by_order_no(self, order_no)` | order_no: 订单号 | `ReconOrderMain`：订单主记录 | 根据订单号查询对账订单主记录 |
| `get_order_split_sub_by_order_no(self, order_no)` | order_no: 订单号 | `List[ReconOrderSplitSub]`：分账子记录列表 | 根据订单号查询分账子记录 |
| `get_pending_recon_orders(self, date_str, offset, limit)` | date_str: 日期<br>offset: 偏移量<br>limit: 限制数量 | `List[ReconOrderMain]`：待核账订单列表 | 查询指定日期的待核账订单（分页） |
| `update_recon_status(self, order_no, recon_status)` | order_no: 订单号<br>recon_status: 对账状态 | `bool`：更新结果 | 更新对账状态 |

### 1.6 dialect.recon_database_dialect

**描述**：数据库方言模块，处理不同数据库的语法差异。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `create_dialect(connection)` | connection: 数据库连接 | `ReconDatabaseDialect`：数据库方言实例 | 根据数据库连接创建对应的数据库方言实例 |

#### 1.6.1 ReconDatabaseDialect

**描述**：数据库方言接口，定义了数据库相关的方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `get_insert_order_main_sql(self)` | 无 | `str`：插入订单主记录的 SQL 语句 | 获取插入订单主记录的 SQL 语句 |
| `get_insert_order_split_sub_sql(self)` | 无 | `str`：插入分账子记录的 SQL 语句 | 获取插入分账子记录的 SQL 语句 |
| `get_insert_exception_sql(self)` | 无 | `str`：插入异常记录的 SQL 语句 | 获取插入异常记录的 SQL 语句 |
| `get_select_order_main_by_order_no_sql(self)` | 无 | `str`：根据订单号查询订单主记录的 SQL 语句 | 获取根据订单号查询订单主记录的 SQL 语句 |
| `get_select_order_split_sub_by_order_no_sql(self)` | 无 | `str`：根据订单号查询分账子记录的 SQL 语句 | 获取根据订单号查询分账子记录的 SQL 语句 |
| `get_select_pending_recon_orders_sql(self)` | 无 | `str`：查询待核账订单的 SQL 语句 | 获取查询待核账订单的 SQL 语句 |
| `get_update_recon_status_sql(self)` | 无 | `str`：更新对账状态的 SQL 语句 | 获取更新对账状态的 SQL 语句 |

#### 1.6.2 MySQLDialect

**描述**：MySQL 数据库方言实现。

**方法**：实现了 `ReconDatabaseDialect` 接口的所有方法。

#### 1.6.3 PostgreSQLDialect

**描述**：PostgreSQL 数据库方言实现。

**方法**：实现了 `ReconDatabaseDialect` 接口的所有方法。

## 2. 实体模块

### 2.1 entity.recon_order_main

**描述**：对账订单主记录实体类。

#### 2.1.1 ReconOrderMain

**描述**：对账订单主记录实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `id` | `int` | 主键 |
| `order_no` | `str` | 订单号 |
| `merchant_id` | `str` | 商户ID |
| `merchant_name` | `str` | 商户名称 |
| `order_amount` | `float` | 订单金额 |
| `actual_amount` | `float` | 实际金额 |
| `recon_status` | `int` | 对账状态（0: 待对账, 1: 已对账, 2: 对账异常） |
| `order_time` | `datetime` | 订单时间 |
| `pay_time` | `datetime` | 支付时间 |
| `recon_time` | `datetime` | 对账时间 |
| `create_time` | `datetime` | 创建时间 |
| `update_time` | `datetime` | 更新时间 |

### 2.2 entity.recon_order_split_sub

**描述**：对账订单分账子记录实体类。

#### 2.2.1 ReconOrderSplitSub

**描述**：对账订单分账子记录实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `id` | `int` | 主键 |
| `order_no` | `str` | 订单号 |
| `sub_order_no` | `str` | 分账子订单号 |
| `merchant_id` | `str` | 商户ID |
| `split_amount` | `float` | 分账金额 |
| `status` | `int` | 状态（0: 待处理, 1: 已处理, 2: 处理异常） |
| `create_time` | `datetime` | 创建时间 |
| `update_time` | `datetime` | 更新时间 |

### 2.3 entity.recon_exception

**描述**：对账异常记录实体类。

#### 2.3.1 ReconException

**描述**：对账异常记录实体类。

**字段**：

| 字段名 | 类型 | 描述 |
|--------|------|------|
| `id` | `int` | 主键 |
| `order_no` | `str` | 订单号 |
| `merchant_id` | `str` | 商户ID |
| `exception_type` | `int` | 异常类型（1: 金额不匹配, 2: 订单不存在, 3: 其他异常） |
| `exception_msg` | `str` | 异常消息 |
| `exception_step` | `int` | 异常步骤（1: 数据获取, 2: 数据匹配, 3: 状态更新） |
| `create_time` | `datetime` | 创建时间 |
| `update_time` | `datetime` | 更新时间 |

## 3. 工具模块

### 3.1 util.recon_util

**描述**：对账工具模块，提供对账相关的工具方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `is_amount_match(expected, actual, tolerance)` | expected: 期望金额<br>actual: 实际金额<br>tolerance: 容差 | `bool`：是否匹配 | 检查金额是否匹配 |
| `format_date(date_obj, pattern)` | date_obj: 日期对象<br>pattern: 日期格式 | `str`：格式化后的日期 | 格式化日期 |
| `parse_date(date_str, pattern)` | date_str: 日期字符串<br>pattern: 日期格式 | `datetime`：解析后的日期 | 解析日期字符串 |
| `generate_order_no()` | 无 | `str`：订单号 | 生成订单号 |
| `generate_sub_order_no()` | 无 | `str`：分账子订单号 | 生成分账子订单号 |

### 3.2 util.encrypt_util

**描述**：加密工具模块，提供配置加密和解密方法。

**方法**：

| 方法名 | 参数 | 返回值 | 描述 |
|--------|------|--------|------|
| `encrypt(plain_text, secret_key)` | plain_text: 明文<br>secret_key: 密钥 | `str`：加密后的字符串 | 加密字符串 |
| `decrypt(encrypted_text, secret_key)` | encrypted_text: 加密后的字符串<br>secret_key: 密钥 | `str`：解密后的明文 | 解密字符串 |

## 4. 配置

### 4.1 数据库配置

**示例**：

```python
# 数据库连接配置
db_config = {
    'host': 'localhost',
    'user': 'root',
    'password': 'password',
    'database': 'easy_recon',
    'port': 3306,
    'pool_size': 10,
    'charset': 'utf8mb4'
}

# 初始化数据库连接
import mysql.connector
from mysql.connector import pooling

connection_pool = mysql.connector.pooling.MySQLConnectionPool(
    pool_name="recon_pool",
    pool_size=10,
    **db_config
)

# 获取连接
connection = connection_pool.get_connection()
```

### 4.2 线程池配置

**示例**：

```python
# 线程池配置
from concurrent.futures import ThreadPoolExecutor

thread_pool = ThreadPoolExecutor(
    max_workers=10,
    thread_name_prefix="recon_"
)
```

### 4.3 告警配置

**示例**：

```python
# 告警配置
alarm_config = {
    'type': 'log',
    'dingtalk': {
        'webhook_url': 'https://oapi.dingtalk.com/robot/send?access_token=YOUR_TOKEN'
    }
}

# 使用日志告警
from service.alarm_service import AlarmService, LogAlarmStrategy

alarm_service = AlarmService(LogAlarmStrategy())

# 使用钉钉告警
from service.alarm_service import DingTalkAlarmStrategy

dingtalk_strategy = DingTalkAlarmStrategy(alarm_config['dingtalk']['webhook_url'])
alarm_service = AlarmService(dingtalk_strategy)
```

## 5. 错误处理

**示例**：

```python
# 错误处理
try:
    result = template.do_realtime_recon(order_main, split_subs)
    if result:
        print("对账成功")
    else:
        print("对账失败")
except Exception as e:
    # 处理异常
    print(f"对账异常: {str(e)}")
    # 记录日志
    import logging
    logging.error(f"对账异常: {str(e)}")
```

## 6. 异步处理

**示例**：

```python
import asyncio
from concurrent.futures import ThreadPoolExecutor

async def async_process_orders(template, orders):
    """异步处理多个订单"""
    results = []
    
    # 使用线程池执行同步操作
    with ThreadPoolExecutor(max_workers=10) as executor:
        loop = asyncio.get_event_loop()
        tasks = []
        
        for order in orders:
            # 创建分账子记录
            split_subs = []
            sub = ReconOrderSplitSub()
            sub.order_no = order.order_no
            sub.sub_order_no = f"SUB_{int(time.time())}"
            sub.merchant_id = order.merchant_id
            sub.split_amount = order.order_amount * 0.8
            sub.status = 0
            sub.create_time = datetime.now()
            sub.update_time = datetime.now()
            split_subs.append(sub)
            
            # 提交任务到线程池
            task = loop.run_in_executor(
                executor,
                template.do_realtime_recon,
                order,
                split_subs
            )
            tasks.append(task)
        
        # 等待所有任务完成
        results = await asyncio.gather(*tasks)
    
    return results

# 使用示例
async def main():
    # 初始化模板...
    
    # 创建多个订单
    orders = []
    for i in range(10):
        order = ReconOrderMain()
        order.order_no = f"ORDER_{int(time.time())}_{i}"
        order.merchant_id = "MERCHANT_001"
        order.merchant_name = "测试商户"
        order.order_amount = 100.00
        order.actual_amount = 100.00
        order.recon_status = 0
        order.order_time = datetime.now()
        order.pay_time = datetime.now()
        order.create_time = datetime.now()
        order.update_time = datetime.now()
        orders.append(order)
    
    # 异步处理
    results = await async_process_orders(template, orders)
    print(f"处理结果: {results}")

# 运行异步函数
if __name__ == "__main__":
    asyncio.run(main())
```

## 7. 最佳实践

### 7.1 数据库连接管理

- 使用连接池管理数据库连接
- 及时关闭不再使用的连接
- 处理数据库连接异常

### 7.2 错误处理

- 使用 try-except 捕获异常
- 记录详细的错误信息
- 区分不同类型的错误

### 7.3 异步处理

- 对于批量操作，使用异步处理提高性能
- 合理设置并发数，避免系统过载
- 处理异步操作中的异常

### 7.4 配置管理

- 使用配置文件或环境变量管理配置
- 对敏感配置进行加密
- 提供合理的默认值

### 7.5 日志管理

- 使用结构化日志
- 设置适当的日志级别
- 记录关键操作和错误信息

### 7.6 性能优化

- 使用批量操作减少数据库交互
- 合理使用缓存
- 优化查询语句
