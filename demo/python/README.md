# Easy Recon Python Demo

Easy Recon SDK 的 Python 演示程序，展示了 11 种核心对账场景。

## 功能场景

1.  **同步实时对账**: 演示基础的支付、分账、通知状态的实时对账。
2.  **退款对账**: 演示订单发生退款时的对账处理。
3.  **异步对账**: 演示先创建订单，后通过异步线程进行退款或其他操作的场景。
4.  **异常处理**: 模拟金额不匹配等业务异常，展示 SDK 的异常记录功能。
5.  **定时核账**: 演示如何手动触发指定日期的全量离线核账任务 (Stub)。
6.  **报表查询**: (预留) 演示对账汇总和明细查询。
7.  **商户通知状态逻辑**: 演示通知状态从 "处理中" 变更为 "成功" 时，对账状态的自动流转。
8.  **通知回调 API**: 演示使用 `recon_notify` 接口处理异步回调通知。
9.  **多商户闭环通知**: 演示多子订单分属不同商户时，如何逐个确认通知结果并最终完成主单对账。
10. **子订单退款对账**: 演示仅凭 `merchant_id` 和 `sub_order_no` 进行退款对账。
11. **原始单号对账**: 演示使用商户系统的原始单号 (`merchant_order_no`) 进行全流程核账。

## 运行与配置

### 1. 环境准备
- Python 3.7+
- MySQL 数据库

### 2. 依赖安装
```bash
pip install -r requirements.txt
```

### 3. 配置数据库
打开 `main.py`，修改 `ReconConfig` 中的数据库连接信息：

```python
self.config = ReconConfig(
    db_host="localhost",
    db_port=3306,
    db_user="root",
    db_password="your_password",
    db_name="easy_recon",
    auto_create_table=True  # 开启自动建表
)
```

### 4. 运行 Demo
```bash
python main.py
```
或者使用脚本:
```bash
sh run_demo.sh
```

## 注意事项
- 确保数据库服务已启动且配置的 Database 存在（表会自动创建）。
- Demo 中生成的订单号带有时间戳，每次运行都会产生新数据。
