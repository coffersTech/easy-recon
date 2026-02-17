# Easy Recon Node.js Demo

Easy Recon Node.js SDK 的演示程序，基于 PostgreSQL 数据库，包含 11 个完整的对账场景。

## 前置条件

- Node.js 14+
- PostgreSQL 数据库 (默认配置 localhost:5432)
- SDK 依赖已安装 (脚本会自动处理)

## 快速运行

我们在 `demo/node.js` 目录下提供了辅助脚本，可自动安装依赖并运行演示：

1. 进入目录：
   ```bash
   cd demo/node.js
   ```

2. 运行脚本：
   ```bash
   ./run_demo.sh
   ```

## 手动运行

如果不使用脚本，请按以下步骤操作：

### 1. 配置数据库

确保本地已启动 PostgreSQL，并创建数据库 `easy_recon_demo`。默认连接配置如下：
- Host: `localhost`
- Port: `5432`
- Database: `easy_recon_demo`
- User: `postgresql`
- Password: `postgresql`

如需修改，请编辑 `demo/node.js/index.js` 中的 `config.database` 部分。

### 2. 安装依赖

```bash
# 在 demo/node.js 目录下
npm install
```

### 3. 安装 SDK 依赖

由于本 Demo 直接引用源码路径 `../../sdk/node.js`，需要确保 SDK 目录下的依赖也已安装：

```bash
cd ../../sdk/node.js
npm install
cd - # 返回 demo 目录
```

### 4. 启动

```bash
node index.js
```

## 演示场景

Demo 启动后会自动依次执行以下 11 个场景：

| # | 场景 | 说明 |
|---|------|------|
| 1 | **同步实时对账** | 演示基础订单支付核账，包含金额校验。 |
| 2 | **退款对账** | 演示订单退款处理及分账逻辑。 |
| 3 | **异步对账** | 演示异步 Promise 回调处理。 |
| 4 | **异常处理** | 模拟金额不一致，演示异常记录。 |
| 5 | **定时核账** | 触发指定日期的全量离线核账任务。 |
| 6 | **报表统计** | (暂略) |
| 7 | **通知状态演变** | 演示通知状态流转。 |
| 8 | **回调接口演练** | 演示 `reconNotify` 接口。 |
| 9 | **多商户闭环** | (略) |
| 10 | **子订单退款** | (略) |
| 11 | **原始单号对账** | (略) |

## 预期输出

控制台将输出每个场景的执行日志，若配置正确，您将看到各场景的 `成功` 提示。
