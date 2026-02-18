# Python 集成指南

## 环境要求

- Python 3.7 或更高版本
- MySQL 5.7+ 或 PostgreSQL 10+
- pip 包管理工具

## 集成步骤

### 1. 安装 SDK

使用 pip 安装 Easy Recon SDK：

```bash
pip install easy-recon-sdk
```

### 2. 初始化项目

```bash
mkdir -p my-recon-app/src
cd my-recon-app
```

### 3. 配置数据源

创建配置文件 `config/config.py`：

```python
import os

class DBConfig:
    def __init__(self):
        self.host = os.getenv('DB_HOST', 'localhost')
        self.port = int(os.getenv('DB_PORT', '3306'))
        self.user = os.getenv('DB_USER', 'root')
        self.password = os.getenv('DB_PASSWORD', 'password')
        self.database = os.getenv('DB_DATABASE', 'easy_recon')
        self.pool_size = int(os.getenv('DB_POOL_SIZE', '10'))

    def get_connection_params(self):
        return {
            'host': self.host,
            'user': self.user,
            'password': self.password,
            'database': self.database,
            'port': self.port
        }

class AlarmConfig:
    def __init__(self):
        self.type = os.getenv('ALARM_TYPE', 'log')
        self.dingtalk = {
            'webhook_url': os.getenv('DINGTALK_WEBHOOK', '')
        }

db_config = DBConfig()
alarm_config = AlarmConfig()
```

### 7. 初始化 SDK

创建初始化文件 `src/core/init.py`：

```python
from repository.recon_repository import ReconRepository
from core.easy_recon_factory import EasyReconFactory
from service.alarm_service import AlarmService, LogAlarmStrategy, DingTalkAlarmStrategy
from service.realtime_recon_service import RealtimeReconService
from service.timing_recon_service import TimingReconService
from core.easy_recon_template import EasyReconTemplate
import mysql.connector
from config import db_config, alarm_config


def init_recon_service():
    """初始化对账服务"""
    # 1. 连接数据库
    connection = mysql.connector.connect(**db_config.get_connection_params())
    
    # 2. 创建 ReconConfig (如果使用 Factory)
    # 或者手动创建
    repo = ReconRepository(config) # 需先初始化 config
    
    # 推荐使用 Factory (假设 SDK 提供了便捷入口，参考 Demo)
    # 这里演示手动组装，与 SDK 保持一致
    
    # 4. 创建告警服务
    if alarm_config.type == 'dingtalk' and alarm_config.dingtalk['webhook_url']:
        alarm_strategy = DingTalkAlarmStrategy(alarm_config.dingtalk['webhook_url'])
    else:
        alarm_strategy = LogAlarmStrategy()
    
    alarm_service = AlarmService(alarm_strategy)
    
    # 5. 创建对账服务
    realtime_service = RealtimeReconService(repo, alarm_service)
    timing_service = TimingReconService(repo, alarm_service)
    
    # 6. 创建模板
    template = EasyReconTemplate(realtime_service, timing_service)
    
    print("Recon service initialized successfully")
    return template
```

### 8. 使用 SDK

创建主文件 `src/main.py`：

```python
from core.init import init_recon_service
from easy_recon_sdk.entity.recon_order_main import ReconOrderMain
from easy_recon_sdk.entity.recon_order_split_sub import ReconOrderSplitSub
from datetime import datetime, date, timedelta


def main():
    """主函数"""
    # 1. 初始化 SDK
    template = init_recon_service()
    
    # 2. 测试实时对账
    print("Testing real-time reconciliation...")
    test_realtime_recon(template)
    
    # 3. 测试定时对账
    print("Testing timing reconciliation...")
    test_timing_recon(template)
    
    print("Integration test completed")


def test_realtime_recon(template):
    """测试实时对账"""
    # 创建订单主记录
    order_main = ReconOrderMain(
        order_no=f"ORDER_{int(datetime.now().timestamp())}",
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
    
    # 创建分账子记录
    split_subs = [
        ReconOrderSplitSub(
            order_no=order_main.order_no,
            sub_order_no=f"SUB_{int(datetime.now().timestamp())}",
            merchant_id="MERCHANT_001",
            split_amount=80.00,
            status=0,
            create_time=datetime.now(),
            update_time=datetime.now()
        )
    ]
    
    # 执行对账
    result = template.do_realtime_recon(order_main, split_subs)
    if result:
        print("Real-time recon succeeded")
    else:
        print("Real-time recon failed")


def test_timing_recon(template):
    """测试定时对账"""
    # 执行定时对账
    date_str = (date.today() - timedelta(days=1)).strftime("%Y-%m-%d")
    result = template.do_timing_recon(date_str)
    if result:
        print("Timing recon succeeded")
    else:
        print("Timing recon failed")


if __name__ == "__main__":
    main()
```

### 9. 异步处理示例

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
            sub = ReconOrderSplitSub(
                order_no=order.order_no,
                sub_order_no=f"SUB_{int(datetime.now().timestamp())}",
                merchant_id=order.merchant_id,
                split_amount=order.order_amount * 0.8,
                status=0,
                create_time=datetime.now(),
                update_time=datetime.now()
            )
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


async def main_async():
    """异步主函数"""
    template = init_recon_service()
    
    # 创建多个订单
    orders = []
    for i in range(10):
        order = ReconOrderMain(
            order_no=f"ORDER_{int(datetime.now().timestamp())}_{i}",
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
        orders.append(order)
    
    # 异步处理
    results = await async_process_orders(template, orders)
    print(f"处理结果: {results}")
    print(f"成功数: {sum(results)}")
    print(f"失败数: {len(results) - sum(results)}")


if __name__ == "__main__":
    asyncio.run(main_async())
```

## 配置管理

### 1. 环境变量配置

| 环境变量 | 描述 | 默认值 |
|---------|------|-------|
| DB_HOST | 数据库主机 | localhost |
| DB_PORT | 数据库端口 | 3306 |
| DB_USER | 数据库用户 | root |
| DB_PASSWORD | 数据库密码 | password |
| DB_DATABASE | 数据库名称 | easy_recon |
| DB_POOL_SIZE | 连接池大小 | 10 |
| ALARM_TYPE | 告警类型（log 或 dingtalk） | log |
| DINGTALK_WEBHOOK | 钉钉告警 Webhook URL | - |

### 2. 配置文件

也可以使用 YAML 或 JSON 格式的配置文件：

```python
# 使用 YAML 配置
import yaml

with open('config.yaml', 'r') as f:
    config = yaml.safe_load(f)

db_host = config['database']['host']
db_port = config['database']['port']
# 其他配置...
```

## 错误处理

### 1. 基本错误处理

```python
try:
    template = init_recon_service()
    result = template.do_realtime_recon(order_main, split_subs)
    if result:
        print("Recon succeeded")
    else:
        print("Recon failed")
except Exception as e:
    print(f"Error: {str(e)}")
    # 处理错误，例如重试或告警
```

### 2. 自定义异常

定义自定义异常以便更好地处理不同类型的错误：

```python
class ReconError(Exception):
    """对账错误基类"""
    pass

class DatabaseError(ReconError):
    """数据库错误"""
    pass

class AmountMismatchError(ReconError):
    """金额不匹配错误"""
    pass

class OrderNotFoundError(ReconError):
    """订单不存在错误"""
    pass

# 错误处理示例
try:
    result = template.do_realtime_recon(order_main, split_subs)
except DatabaseError as e:
    print(f"Database error: {str(e)}")
except AmountMismatchError as e:
    print(f"Amount mismatch: {str(e)}")
except OrderNotFoundError as e:
    print(f"Order not found: {str(e)}")
except ReconError as e:
    print(f"Recon error: {str(e)}")
except Exception as e:
    print(f"Unknown error: {str(e)}")
```

## 监控与维护

### 1. 日志管理

使用 Python 的 logging 模块进行日志管理：

```python
import logging
import json
from datetime import datetime

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler("recon.log"),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger("recon")

# 结构化日志
def log_recon_result(order_no, success, error=None):
    """记录对账结果"""
    log_data = {
        "timestamp": datetime.now().isoformat(),
        "order_no": order_no,
        "success": success,
        "error": error
    }
    if success:
        logger.info(json.dumps(log_data))
    else:
        logger.error(json.dumps(log_data))

# 使用示例
try:
    result = template.do_realtime_recon(order_main, split_subs)
    log_recon_result(order_main.order_no, result)
except Exception as e:
    log_recon_result(order_main.order_no, False, str(e))
```

### 2. 健康检查

实现健康检查函数，监控系统状态：

```python
def health_check():
    """健康检查"""
    try:
        # 检查数据库连接
        import mysql.connector
        from config import db_config
        
        connection = mysql.connector.connect(**db_config.get_connection_params())
        connection.close()
        db_status = "ok"
    except Exception as e:
        db_status = f"error: {str(e)}"
    
    return {
        "status": "ok",
        "timestamp": datetime.now().isoformat(),
        "components": {
            "database": db_status
        }
    }

# 使用示例
print(json.dumps(health_check(), indent=2))
```

## 部署

### 1. 直接运行

```bash
# 设置环境变量
export DB_HOST=localhost
export DB_PORT=3306
export DB_USER=root
export DB_PASSWORD=password
export DB_DATABASE=easy_recon

# 运行应用
python src/main.py
```

### 2. Docker 部署

创建 `Dockerfile`：

```dockerfile
FROM python:3.9-slim

WORKDIR /app

COPY . .

RUN pip install --no-cache-dir -r requirements.txt

EXPOSE 8080

ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_USER=root
ENV DB_PASSWORD=password
ENV DB_DATABASE=easy_recon

CMD ["python", "src/main.py"]
```

构建并运行 Docker 容器：

```bash
# 构建镜像
docker build -t recon-app .

# 运行容器
docker run -d --name recon-app \
  -e DB_HOST=mysql \
  -e DB_PORT=3306 \
  -e DB_USER=root \
  -e DB_PASSWORD=password \
  -e DB_DATABASE=easy_recon \
  recon-app
```

## 常见问题与解决方案

### 1. 数据库连接失败

**原因**：
- 数据库服务未启动
- 连接信息配置错误
- 网络连接问题

**解决方案**：
- 检查数据库服务状态
- 验证连接信息
- 检查网络连接
- 增加连接超时设置

### 2. 对账结果不准确

**原因**：
- 数据格式错误
- 金额计算错误
- 并发访问导致数据不一致

**解决方案**：
- 检查数据格式
- 验证金额计算
- 使用事务保证数据一致性
- 增加数据验证

### 3. 性能问题

**原因**：
- 线程池配置不合理
- 数据库查询慢
- 并发度不够

**解决方案**：
- 调整线程池大小
- 优化数据库索引
- 增加并发度
- 使用缓存减少数据库访问

### 4. 告警不触发

**原因**：
- 告警配置错误
- 网络问题
- 告警服务未启动

**解决方案**：
- 检查告警配置
- 验证网络连接
- 测试告警通道
- 增加告警日志

## 最佳实践

1. **使用虚拟环境**：隔离项目依赖
2. **错误处理**：使用 try-except 捕获并处理异常
3. **异步处理**：对于批量操作，使用异步处理提高性能
4. **监控告警**：配置合适的告警方式，及时发现异常
5. **配置管理**：使用环境变量或配置文件管理配置
6. **日志管理**：使用结构化日志，便于分析问题
7. **健康检查**：实现健康检查函数，监控系统状态
8. **性能测试**：在生产环境部署前进行性能测试
9. **备份数据**：定期备份数据库，防止数据丢失
10. **代码组织**：按照功能模块组织代码，提高可维护性

## 示例项目

完整的示例项目代码可参考：
- GitHub: https://github.com/example/easy-recon-sdk-examples
