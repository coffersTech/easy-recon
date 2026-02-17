# Easy Recon SDK for Python

Easy Recon 是一个轻量级的对账 SDK，支持实时对账和定时对账策略，旨在帮助开发者快速集成标准化的核账能力。

## 核心特性

- **实时对账**: 在支付和通知及退款发生时立即进行对账，保证数据一致性。
- **定时对账**: 支持后台定时任务触发的离线核账，作为兜底机制。
- **退款支持**: 支持全额退款和部分退款的核账逻辑。
- **数据库无关**: 基于标准 SQL 构建，兼容 MySQL 和 PostgreSQL。
- **自动建表**: 支持 SDK 初始化时自动创建所需的数据库表结构。

## 安装

```bash
pip install easy-recon-sdk
```

## 快速开始

### 1. 初始化配置

初始化 SDK 并配置数据库连接信息。建议开启自动建表功能以便快速上手。

```python
from config.recon_config import ReconConfig
from core.easy_recon_factory import EasyReconFactory

config = ReconConfig(
    db_host="localhost",
    db_port=3306,
    db_user="root",
    db_password="password",
    db_name="easy_recon",
    auto_create_table=True # 首次运行时自动创建表
)

# 创建 API 客户端实例
easy_recon = EasyReconFactory.create(config)
```

### 2. 订单对账 (Reconcile Order)

在发起支付或分账请求时调用：

```python
from entity.recon_order_split_sub import ReconOrderSplitSub

# 定义分账明细
split_subs = [
    ReconOrderSplitSub(
        sub_order_no="SUB_001",
        merchant_id="M_001",
        split_amount=100.00
    )
]

# 执行实时对账
success = easy_recon.recon_order(
    order_no="ORD_20231027001",
    pay_amount=100.00,
    platform_income=0.00,
    pay_fee=0.00,
    split_details=split_subs,
    pay_status=1,   # 1: 成功
    split_status=1, # 1: 成功
    notify_status=0 # 0: 处理中
)

if success:
    print("对账记录创建成功")
else:
    print("对账记录创建失败")
```

### 3. 处理通知 (Handle Notification)

当收到下游（如网关或子商户系统）的异步通知时调用：

```python
easy_recon.recon_notify(
    order_no="ORD_20231027001",
    merchant_id="M_001",
    notify_url="http://callback.url",
    notify_status=1, # 1: 成功
    notify_result="Success"
)
```

### 4. 退款对账 (Reconcile Refund)

处理退款业务时调用：

```python
easy_recon.recon_refund(
    order_no="ORD_20231027001",
    refund_amount=50.00,
    refund_time=datetime.now(),
    refund_status=1, # 1: 成功
    split_details=...
)
```

## 环境要求

- Python 3.7+
- MySQL 或 PostgreSQL 数据库
