# Go 集成指南

## 环境要求

- Go 1.16 或更高版本
- MySQL 5.7+ 或 PostgreSQL 10+
- Go 模块依赖管理

## 集成步骤

### 1. 创建项目结构

```bash
mkdir -p my-recon-app/{cmd,internal/{config,repository,service,entity,dialect,util},pkg/core}
cd my-recon-app
```

### 2. 初始化 Go 模块

```bash
go mod init my-recon-app
go mod tidy
```

### 3. 添加依赖

在 `go.mod` 文件中添加依赖：

```go
require (
	github.com/go-sql-driver/mysql v1.7.0
	github.com/lib/pq v1.10.9
)
```

### 4. 复制 SDK 代码

将 Easy Recon SDK 的 Go 版本代码复制到项目中，或作为依赖引入。

### 5. 配置数据源

创建配置文件 `config/config.go`：

```go
package config

import (
	"fmt"
	"os"
	"strconv"
)

// DBConfig 数据库配置
type DBConfig struct {
	Host     string
	Port     int
	User     string
	Password string
	Database string
	MaxIdle  int
	MaxOpen  int
}

// AlarmConfig 告警配置
type AlarmConfig struct {
	Type     string
	DingTalk *DingTalkConfig
}

// DingTalkConfig 钉钉告警配置
type DingTalkConfig struct {
	WebhookURL string
}

// LoadDBConfig 加载数据库配置
func LoadDBConfig() *DBConfig {
	port, _ := strconv.Atoi(getEnv("DB_PORT", "3306"))
	maxIdle, _ := strconv.Atoi(getEnv("DB_MAX_IDLE", "10"))
	maxOpen, _ := strconv.Atoi(getEnv("DB_MAX_OPEN", "100"))

	return &DBConfig{
		Host:     getEnv("DB_HOST", "localhost"),
		Port:     port,
		User:     getEnv("DB_USER", "root"),
		Password: getEnv("DB_PASSWORD", "password"),
		Database: getEnv("DB_DATABASE", "easy_recon"),
		MaxIdle:  maxIdle,
		MaxOpen:  maxOpen,
	}
}

// LoadAlarmConfig 加载告警配置
func LoadAlarmConfig() *AlarmConfig {
	return &AlarmConfig{
		Type: getEnv("ALARM_TYPE", "log"),
		DingTalk: &DingTalkConfig{
			WebhookURL: getEnv("DINGTALK_WEBHOOK", ""),
		},
	}
}

// GetDSN 获取数据库连接字符串
func (c *DBConfig) GetDSN(driver string) string {
	switch driver {
	case "mysql":
		return fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?charset=utf8mb4&parseTime=True&loc=Local",
			c.User, c.Password, c.Host, c.Port, c.Database)
	case "postgres":
		return fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=disable",
			c.Host, c.Port, c.User, c.Password, c.Database)
	default:
		return ""
	}
}

// getEnv 获取环境变量，如果不存在则返回默认值
func getEnv(key, defaultValue string) string {
	if value, exists := os.LookupEnv(key); exists {
		return value
	}
	return defaultValue
}
```

### 6. 初始化 SDK

创建初始化文件 `internal/service/init.go`：

```go
package service

import (
	"database/sql"
	"log"

	_ "github.com/go-sql-driver/mysql"
	_ "github.com/lib/pq"

	"my-recon-app/config"
	"my-recon-app/internal/dialect"
	"my-recon-app/internal/entity"
	"my-recon-app/internal/repository"
	"my-recon-app/pkg/core"
)

// InitReconService 初始化对账服务
func InitReconService() (*core.EasyReconTemplate, error) {
	// 1. 加载配置
	dbConfig := config.LoadDBConfig()
	alarmConfig := config.LoadAlarmConfig()

	// 2. 连接数据库
	db, err := sql.Open("mysql", dbConfig.GetDSN("mysql"))
	if err != nil {
		return nil, err
	}

	// 3. 配置连接池
	db.SetMaxIdleConns(dbConfig.MaxIdle)
	db.SetMaxOpenConns(dbConfig.MaxOpen)

	// 4. 测试连接
	if err := db.Ping(); err != nil {
		return nil, err
	}

	// 5. 创建数据库方言
	dbDialect := dialect.CreateDialect(db)

	// 6. 创建存储库
	reconRepo := repository.NewSQLReconRepository(db, dbDialect)

	// 7. 创建告警服务
	var alarmStrategy AlarmStrategy
	if alarmConfig.Type == "dingtalk" && alarmConfig.DingTalk != nil {
		alarmStrategy = NewDingTalkAlarmStrategy(alarmConfig.DingTalk.WebhookURL)
	} else {
		alarmStrategy = &LogAlarmStrategy{}
	}
	alarmService := NewAlarmService(alarmStrategy)

	// 8. 创建对账服务
	realtimeService := NewRealtimeReconService(reconRepo, alarmService)
	timingService := NewTimingReconService(reconRepo, alarmService)

	// 9. 创建模板
	template := core.NewEasyReconTemplate(realtimeService, timingService)

	log.Println("Recon service initialized successfully")
	return template, nil
}
```

### 7. 使用 SDK

创建主文件 `cmd/main.go`：

```go
package main

import (
	"fmt"
	"log"
	"time"

	"my-recon-app/internal/entity"
	"my-recon-app/internal/service"
)

func main() {
	// 1. 初始化 SDK
	template, err := service.InitReconService()
	if err != nil {
		log.Fatalf("Failed to initialize recon service: %v", err)
	}

	// 2. 测试实时对账
	log.Println("Testing real-time reconciliation...")
	testRealtimeRecon(template)

	// 3. 测试定时对账
	log.Println("Testing timing reconciliation...")
	testTimingRecon(template)

	log.Println("Integration test completed")
}

// testRealtimeRecon 测试实时对账
func testRealtimeRecon(template *core.EasyReconTemplate) {
	// 创建订单主记录
	orderMain := &entity.ReconOrderMain{
		OrderNo:      "ORDER_" + fmt.Sprintf("%d", time.Now().Unix()),
		MerchantId:   "MERCHANT_001",
		MerchantName: "测试商户",
		OrderAmount:  100.00,
		ActualAmount: 100.00,
		ReconStatus:  0,
		OrderTime:    time.Now(),
		PayTime:      time.Now(),
		CreateTime:   time.Now(),
		UpdateTime:   time.Now(),
	}

	// 创建分账子记录
	splitSubs := []*entity.ReconOrderSplitSub{
		{
			OrderNo:     orderMain.OrderNo,
			SubOrderNo:  "SUB_" + fmt.Sprintf("%d", time.Now().Unix()),
			MerchantId:  "MERCHANT_001",
			SplitAmount: 80.00,
			Status:      0,
			CreateTime:  time.Now(),
			UpdateTime:  time.Now(),
		},
	}

	// 执行对账
	success, err := template.DoRealtimeRecon(orderMain, splitSubs)
	if err != nil {
		log.Printf("Real-time recon failed: %v", err)
	} else if success {
		log.Println("Real-time recon succeeded")
	} else {
		log.Println("Real-time recon failed")
	}
}

// testTimingRecon 测试定时对账
func testTimingRecon(template *core.EasyReconTemplate) {
	// 执行定时对账
	dateStr := time.Now().AddDate(0, 0, -1).Format("2006-01-02")
	success, err := template.DoTimingRecon(dateStr)
	if err != nil {
		log.Printf("Timing recon failed: %v", err)
	} else if success {
		log.Println("Timing recon succeeded")
	} else {
		log.Println("Timing recon failed")
	}
}
```

### 8. 并发对账示例

```go
// 并发处理多个订单
func processMultipleOrders(template *core.EasyReconTemplate, orders []*entity.ReconOrderMain) {
	var wg sync.WaitGroup
	semaphore := make(chan struct{}, 10) // 限制并发数为10

	for _, order := range orders {
		wg.Add(1)
		semaphore <- struct{}{} // 获取信号量

		go func(o *entity.ReconOrderMain) {
			defer wg.Done()
			defer func() { <-semaphore }() // 释放信号量

			// 执行对账
			success, err := template.DoRealtimeRecon(o, nil)
			if err != nil {
				log.Printf("Recon failed for order %s: %v", o.OrderNo, err)
			} else if success {
				log.Printf("Recon succeeded for order %s", o.OrderNo)
			} else {
				log.Printf("Recon failed for order %s", o.OrderNo)
			}
		}(order)
	}

	wg.Wait()
}
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
| DB_MAX_IDLE | 最大空闲连接数 | 10 |
| DB_MAX_OPEN | 最大打开连接数 | 100 |
| ALARM_TYPE | 告警类型（log 或 dingtalk） | log |
| DINGTALK_WEBHOOK | 钉钉告警 Webhook URL | - |

### 2. 配置文件

也可以使用配置文件管理配置，例如使用 YAML 或 JSON 格式。

## 错误处理

### 1. 基本错误处理

```go	template, err := service.InitReconService()
if err != nil {
	log.Fatalf("Failed to initialize recon service: %v", err)
}

// 执行对账	success, err := template.DoRealtimeRecon(orderMain, splitSubs)
if err != nil {
	log.Printf("Recon failed: %v", err)
	// 处理错误，例如重试或告警
} else if success {
	log.Println("Recon succeeded")
} else {
	log.Println("Recon failed")
	// 处理失败情况
}
```

### 2. 错误类型

定义错误类型以便更好地处理不同类型的错误：

```go
package service

import "errors"

var (
	// ErrDatabase 数据库错误
	ErrDatabase = errors.New("database error")
	// ErrAmountMismatch 金额不匹配错误
	ErrAmountMismatch = errors.New("amount mismatch")
	// ErrOrderNotFound 订单不存在错误
	ErrOrderNotFound = errors.New("order not found")
)

// 错误处理示例
if errors.Is(err, ErrDatabase) {
	// 处理数据库错误
} else if errors.Is(err, ErrAmountMismatch) {
	// 处理金额不匹配错误
} else if errors.Is(err, ErrOrderNotFound) {
	// 处理订单不存在错误
} else {
	// 处理其他错误
}
```

## 监控与维护

### 1. 日志管理

使用结构化日志记录系统运行状态：

```go
package util

import (
	"encoding/json"
	"log"
	"os"
)

// Logger 日志记录器
type Logger struct {
	infoLogger  *log.Logger
	errorLogger *log.Logger
}

// NewLogger 创建新的日志记录器
func NewLogger() *Logger {
	return &Logger{
		infoLogger:  log.New(os.Stdout, "INFO: ", log.Ldate|log.Ltime|log.Lshortfile),
		errorLogger: log.New(os.Stderr, "ERROR: ", log.Ldate|log.Ltime|log.Lshortfile),
	}
}

// Info 记录信息日志
func (l *Logger) Info(message string, data map[string]interface{}) {
	if data == nil {
		l.infoLogger.Println(message)
		return
	}
	data["message"] = message
	jsonData, _ := json.Marshal(data)
	l.infoLogger.Println(string(jsonData))
}

// Error 记录错误日志
func (l *Logger) Error(message string, err error, data map[string]interface{}) {
	if data == nil {
		data = make(map[string]interface{})
	}
	data["message"] = message
	data["error"] = err.Error()
	jsonData, _ := json.Marshal(data)
	l.errorLogger.Println(string(jsonData))
}
```

### 2. 健康检查

实现健康检查接口，监控系统状态：

```go
// HealthCheck 健康检查
func HealthCheck() map[string]interface{} {
	// 检查数据库连接
	db, err := sql.Open("mysql", dbConfig.GetDSN("mysql"))
	defer db.Close()

	dbStatus := "ok"
	if err := db.Ping(); err != nil {
		dbStatus = "error"
	}

	return map[string]interface{}{
		"status":    "ok",
		"timestamp": time.Now().Unix(),
		"components": map[string]interface{}{
			"database": dbStatus,
		},
	}
}
```

## 部署

### 1. 编译

```bash
go build -o recon-app ./cmd/main.go
```

### 2. 运行

```bash
# 设置环境变量
export DB_HOST=localhost
export DB_PORT=3306
export DB_USER=root
export DB_PASSWORD=password
export DB_DATABASE=easy_recon

# 运行应用
./recon-app
```

### 3. Docker 部署

创建 `Dockerfile`：

```dockerfile
FROM golang:1.18-alpine as builder

WORKDIR /app

COPY . .

RUN go mod tidy
RUN go build -o recon-app ./cmd/main.go

FROM alpine:latest

WORKDIR /app

COPY --from=builder /app/recon-app .

EXPOSE 8080

ENV DB_HOST=localhost
ENV DB_PORT=3306
ENV DB_USER=root
ENV DB_PASSWORD=password
ENV DB_DATABASE=easy_recon

CMD ["./recon-app"]
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
- 调整连接池大小
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

1. **使用连接池**：配置合理的数据库连接池大小
2. **错误处理**：使用错误类型区分不同错误，便于处理
3. **并发处理**：使用 goroutine 并发处理多个订单
4. **监控告警**：配置合适的告警方式，及时发现异常
5. **配置管理**：使用环境变量或配置文件管理配置
6. **健康检查**：实现健康检查接口，监控系统状态
7. **日志管理**：使用结构化日志，便于分析问题
8. **性能测试**：在生产环境部署前进行性能测试
9. **备份数据**：定期备份数据库，防止数据丢失
10. **代码组织**：按照标准 Go 项目结构组织代码

## 示例项目

完整的示例项目代码可参考：
- GitHub: https://github.com/example/easy-recon-sdk-examples
