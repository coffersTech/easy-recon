package main

import (
	"database/sql"
	"fmt"
	"log"
	"math/rand"
	"os"
	"sync"
	"time"

	"github.com/coffersTech/easy-recon/sdk/go/config"
	"github.com/coffersTech/easy-recon/sdk/go/core"
	"github.com/coffersTech/easy-recon/sdk/go/dialect"
	"github.com/coffersTech/easy-recon/sdk/go/entity"
	"github.com/coffersTech/easy-recon/sdk/go/repository"
	"github.com/coffersTech/easy-recon/sdk/go/service"
	_ "github.com/go-sql-driver/mysql"
	_ "github.com/lib/pq"
	"github.com/shopspring/decimal"
	"gopkg.in/yaml.v3"
)

// DatabaseConfigWrapper wraps database configuration
type DatabaseConfigWrapper struct {
	Database DatabaseConfig `yaml:"database"`
}

// DatabaseConfig holds database connection details
type DatabaseConfig struct {
	Driver string `yaml:"driver"`
	DSN    string `yaml:"dsn"`
}

func loadDatabaseConfig(path string) (*DatabaseConfig, error) {
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, err
	}
	var wrapper DatabaseConfigWrapper
	err = yaml.Unmarshal(data, &wrapper)
	return &wrapper.Database, err
}

// MockReconRepository with basic in-memory storage for demo purposes
type MockReconRepository struct {
	OrderMap     map[string]*entity.ReconOrderMain
	ExceptionMap map[string][]*entity.ReconException
	SplitSubMap  map[string][]*entity.ReconOrderSplitSub
	mu           sync.Mutex
}

func NewMockReconRepository() *MockReconRepository {
	return &MockReconRepository{
		OrderMap:     make(map[string]*entity.ReconOrderMain),
		ExceptionMap: make(map[string][]*entity.ReconException),
		SplitSubMap:  make(map[string][]*entity.ReconOrderSplitSub),
	}
}

func (m *MockReconRepository) SaveOrderMain(orderMain *entity.ReconOrderMain) (bool, error) {
	m.mu.Lock()
	defer m.mu.Unlock()

	// Create a copy to simulate persistence
	saved := *orderMain
	m.OrderMap[orderMain.OrderNo] = &saved
	fmt.Printf("[MockRepo] Saved Order: %s, Amount: %s, ReconStatus: %d\n", orderMain.OrderNo, orderMain.OrderAmount, orderMain.ReconStatus)
	return true, nil
}

func (m *MockReconRepository) BatchSaveOrderSplitSub(splitSubs []*entity.ReconOrderSplitSub) (bool, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	if len(splitSubs) == 0 {
		return true, nil
	}
	orderNo := splitSubs[0].OrderNo
	// Append to existing subs if any (for simpler map structure)
	m.SplitSubMap[orderNo] = append(m.SplitSubMap[orderNo], splitSubs...)
	fmt.Printf("[MockRepo] Saved %d SplitSubs for Order: %s\n", len(splitSubs), orderNo)
	return true, nil
}

func (m *MockReconRepository) UpdateReconStatus(orderNo string, status int) (bool, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	if order, exists := m.OrderMap[orderNo]; exists {
		order.ReconStatus = status
		m.OrderMap[orderNo] = order
		fmt.Printf("[MockRepo] Updated ReconStatus to %d for Order: %s\n", status, orderNo)
		return true, nil
	}
	// Also check split subs map just in case (though recon status is on main)
	return false, fmt.Errorf("order not found: %s", orderNo)
}

func (m *MockReconRepository) SaveException(exception *entity.ReconException) (bool, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	m.ExceptionMap[exception.OrderNo] = append(m.ExceptionMap[exception.OrderNo], exception)
	fmt.Printf("[MockRepo] Saved Exception for Order: %s, Step: %d, Msg: %s\n", exception.OrderNo, exception.ExceptionStep, exception.ExceptionMsg)
	return true, nil
}

func (m *MockReconRepository) BatchSaveException(exceptions []*entity.ReconException) (bool, error) {
	for _, ex := range exceptions {
		m.SaveException(ex)
	}
	return true, nil
}

func (m *MockReconRepository) GetOrderMainByOrderNo(orderNo string) (*entity.ReconOrderMain, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	if order, exists := m.OrderMap[orderNo]; exists {
		return order, nil
	}
	return nil, nil // Not found
}

func (m *MockReconRepository) GetOrderSplitSubByOrderNo(orderNo string) ([]*entity.ReconOrderSplitSub, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	if subs, exists := m.SplitSubMap[orderNo]; exists {
		return subs, nil
	}
	return []*entity.ReconOrderSplitSub{}, nil
}

func (m *MockReconRepository) GetPendingReconOrders(dateStr string, offset, limit int) ([]*entity.ReconOrderMain, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	var pending []*entity.ReconOrderMain
	// Mock implementation: Iterate all and find those with ReconStatus == 0 (Pending)
	for _, order := range m.OrderMap {
		if order.ReconStatus == entity.ReconStatusPending {
			pending = append(pending, order)
		}
	}
	return pending, nil
}

// Other Mock methods
func (m *MockReconRepository) GetOrderMainByMerchantId(merchantId, startDate, endDate string, reconStatus *int, offset, limit int) ([]*entity.ReconOrderMain, error) {
	return nil, nil
}
func (m *MockReconRepository) GetOrderMainByDate(dateStr string, reconStatus *int, offset, limit int) ([]*entity.ReconOrderMain, error) {
	return nil, nil
}
func (m *MockReconRepository) GetExceptionRecords(merchantId, startDate, endDate string, exceptionStep *int, offset, limit int) ([]*entity.ReconException, error) {
	return nil, nil
}
func (m *MockReconRepository) GetExceptionByOrderNo(orderNo string) (*entity.ReconException, error) {
	return nil, nil
}
func (m *MockReconRepository) SaveSummary(summary *entity.ReconSummary) (bool, error) {
	fmt.Printf("[MockRepo] Saved ReconSummary: Date=%v\n", summary.SummaryDate)
	return true, nil
}
func (m *MockReconRepository) SaveNotifyLog(notifyLog *entity.ReconNotifyLog) (bool, error) {
	fmt.Printf("[MockRepo] Saved NotifyLog: Url=%s, Status=%d\n", notifyLog.NotifyUrl, notifyLog.NotifyStatus)
	return true, nil
}

func (m *MockReconRepository) BatchSaveOrderRefundSplitSub(refundSplitSubs []*entity.ReconOrderRefundSplitSub) (bool, error) {
	if len(refundSplitSubs) == 0 {
		return true, nil
	}
	fmt.Printf("[MockRepo] Saved %d RefundSplitSubs\n", len(refundSplitSubs))
	return true, nil
}

func (m *MockReconRepository) GetOrderMainByMerchantOrderNo(merchantId, merchantOrderNo string) (*entity.ReconOrderMain, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	for _, order := range m.OrderMap {
		if order.MerchantId == merchantId && order.MerchantOrderNo == merchantOrderNo {
			return order, nil
		}
	}
	return nil, nil // Not found
}

func (m *MockReconRepository) GetOrderSplitSubBySubOrderNo(merchantId, subOrderNo string) (*entity.ReconOrderSplitSub, error) {
	m.mu.Lock()
	defer m.mu.Unlock()
	// Mock: iterating through all split subs in map (which is map[orderNo][])
	for _, subs := range m.SplitSubMap {
		for _, sub := range subs {
			if sub.MerchantId == merchantId && sub.SubOrderNo == subOrderNo {
				return sub, nil
			}
		}
	}
	return nil, nil
}

func main() {
	fmt.Println("=== Easy Recon SDK Go Demo Start ===")

	// 1. Initialize SDK
	configFile := "config.yaml"
	cfg, err := config.LoadConfig(configFile)
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	// 2. Load Database Config
	dbConfig, err := loadDatabaseConfig(configFile)
	if err != nil {
		log.Printf("Failed to load database config (using mock): %v", err)
	}

	var repo repository.ReconRepository
	if dbConfig != nil && dbConfig.Driver != "" && dbConfig.DSN != "" {
		fmt.Printf("Connecting to database: %s\n", dbConfig.Driver)
		db, err := sql.Open(dbConfig.Driver, dbConfig.DSN)
		if err != nil {
			log.Fatalf("Failed to open database: %v", err)
		}
		if err := db.Ping(); err != nil {
			log.Fatalf("Failed to ping database: %v", err)
		}

		// Drop existing tables for clean schema (demo only)
		dropTables := []string{
			"easy_recon_order_main",
			"easy_recon_order_split_sub",
			"easy_recon_exception",
			"easy_recon_summary",
			"easy_recon_notify_log",
			"easy_recon_order_refund_split_sub",
		}
		for _, t := range dropTables {
			db.Exec("DROP TABLE IF EXISTS " + t + " CASCADE")
		}

		// Create dialect and repo
		d := dialect.CreateDialect(db)
		repo = repository.NewSQLReconRepository(db, d, cfg)
		fmt.Println("Using SQL Recon Repository")
	} else {
		repo = NewMockReconRepository()
		fmt.Println("Using Mock Recon Repository")
	}

	alarmService := service.NewAlarmService(cfg)
	exceptionService := service.NewExceptionRecordService(repo)

	realtimeService := service.NewRealtimeReconService(repo, exceptionService, alarmService, cfg)
	timingService := service.NewTimingReconService(repo, exceptionService, alarmService, cfg)

	template := core.NewEasyReconTemplate(realtimeService, timingService)

	// 2. Run Scenarios
	runScenario("1. Sync Realtime Recon (Success)", func() {
		demoSyncRecon(template, repo)
	})
	runScenario("2. Sync Realtime Recon (Exception)", func() {
		demoExceptionHandling(template, repo)
	})
	runScenario("3. Timing Recon", func() {
		demoTimingRecon(template, repo)
	})
	runScenario("4. Recon Refund", func() {
		demoReconRefund(template, repo, false)
	})
	runScenario("5. Recon Refund Async", func() {
		demoReconRefund(template, repo, true)
	})
	runScenario("6. Recon Notify", func() {
		demoReconNotify(template, repo)
	})
	runScenario("7. Recon Notify By Sub Order", func() {
		demoReconNotifyBySub(template, repo)
	})
	runScenario("8. Recon Notify By Merchant Order", func() {
		demoReconNotifyByMerchantOrder(template, repo)
	})
	runScenario("9. Recon Refund By Sub", func() {
		demoRefundBySub(template, repo)
	})
	runScenario("10. Recon Refund By Merchant Order", func() {
		demoRefundByMerchantOrder(template, repo)
	})
	runScenario("11. Query Status & Summary", func() {
		demoQuery(template)
	})

	fmt.Println("\n=== Easy Recon SDK Go Demo End ===")
}

func runScenario(name string, f func()) {
	fmt.Printf("\n--- [Scenario] %s ---\n", name)
	f()
	time.Sleep(100 * time.Millisecond) // separate logs
}

// 1. Sync Realtime Recon (Success)
func demoSyncRecon(template *core.EasyReconTemplate, repo repository.ReconRepository) {
	orderNo := generateOrderNo("SYNC")
	payAmount := decimal.NewFromFloat(300.00)

	// Save main order first? No, ReconOrder saves it.
	// But to match Notify scenarios later, we might need data.
	// For standard flow, we just call ReconOrder.

	splitSubs := []*entity.ReconOrderSplitSub{
		{SubOrderNo: orderNo + "-1", MerchantId: "MCH-001", SplitAmount: decimal.NewFromFloat(200.00)},
		{SubOrderNo: orderNo + "-2", MerchantId: "MCH-002", SplitAmount: decimal.NewFromFloat(100.00)},
	}

	result := template.ReconOrder(
		orderNo,
		payAmount,
		decimal.Zero,
		decimal.Zero,
		splitSubs,
		entity.PayStatusSuccess,
		entity.SplitStatusSuccess,
		entity.NotifyStatusSuccess,
	)

	printResult(orderNo, result.Success, result.Msg)
}

// 2. Exception Handling
func demoExceptionHandling(template *core.EasyReconTemplate, repo repository.ReconRepository) {
	orderNo := generateOrderNo("ERR")
	payAmount := decimal.NewFromFloat(100.00)
	splitSubs := []*entity.ReconOrderSplitSub{
		{SubOrderNo: orderNo + "-1", MerchantId: "MCH-001", SplitAmount: decimal.NewFromFloat(60.00)},
		// Missing 40.00
	}

	result := template.ReconOrder(
		orderNo,
		payAmount,
		decimal.Zero,
		decimal.Zero,
		splitSubs,
		entity.PayStatusSuccess,
		entity.SplitStatusSuccess,
		entity.NotifyStatusSuccess,
	)

	printResult(orderNo, result.Success, result.Msg)
}

// 3. Timing Recon
func demoTimingRecon(template *core.EasyReconTemplate, repo repository.ReconRepository) {
	pendingOrderNo := generateOrderNo("PENDING")
	pendingOrder := &entity.ReconOrderMain{
		OrderNo:      pendingOrderNo,
		OrderAmount:  decimal.NewFromFloat(50.00),
		ReconStatus:  entity.ReconStatusPending,
		PayStatus:    entity.PayStatusSuccess,
		SplitStatus:  entity.SplitStatusSuccess,
		NotifyStatus: entity.NotifyStatusSuccess,
		OrderTime:    time.Now(),
		CreateTime:   time.Now(),
		UpdateTime:   time.Now(),
	}
	repo.SaveOrderMain(pendingOrder)
	repo.BatchSaveOrderSplitSub([]*entity.ReconOrderSplitSub{
		{OrderNo: pendingOrderNo, SubOrderNo: pendingOrderNo + "-S1", MerchantId: "MCH-PEND", SplitAmount: decimal.NewFromFloat(50.00)},
	})

	today := time.Now().Format("2006-01-02")
	success, err := template.DoTimingRecon(today)
	fmt.Printf("Timing Recon Executed. Success: %v, Err: %v\n", success, err)
}

// 4 & 5. Recon Refund
func demoReconRefund(template *core.EasyReconTemplate, repo repository.ReconRepository, async bool) {
	orderNo := "ORD-REFUND-" + fmt.Sprintf("%d", rand.Intn(10000))
	// Setup existing order
	repo.SaveOrderMain(&entity.ReconOrderMain{
		OrderNo:     orderNo,
		OrderAmount: decimal.NewFromFloat(100.00),
		ReconStatus: entity.ReconStatusSuccess, // Already successful
		MerchantId:  "MCH-REF",
	})

	refundAmount := decimal.NewFromFloat(10.00)
	refundSubs := []*entity.ReconOrderRefundSplitSub{
		{OrderNo: orderNo, RefundSplitAmount: refundAmount, MerchantId: "MCH-REF"},
	}

	if async {
		template.ReconRefundAsync(orderNo, refundAmount, time.Now(), 1, refundSubs)
		fmt.Printf("Async refund triggered for %s\n", orderNo)
		time.Sleep(100 * time.Millisecond) // Wait for async execution
	} else {
		res := template.ReconRefund(orderNo, refundAmount, time.Now(), 1, refundSubs)
		printResult(orderNo, res.Success, res.Msg)
	}
}

// 6. Recon Notify
func demoReconNotify(template *core.EasyReconTemplate, repo repository.ReconRepository) {
	orderNo := generateOrderNo("NOTIFY")
	// Setup order
	repo.SaveOrderMain(&entity.ReconOrderMain{
		OrderNo:      orderNo,
		ReconStatus:  entity.ReconStatusPending,
		NotifyStatus: entity.NotifyStatusProcessing,
		MerchantId:   "MCH-NOTIFY",
	})

	res := template.ReconNotify(orderNo, "MCH-NOTIFY", "http://callback.com", entity.NotifyStatusSuccess, "OK")
	printResult(orderNo, res.Success, res.Msg)
}

// 7. Recon Notify By Sub
func demoReconNotifyBySub(template *core.EasyReconTemplate, repo repository.ReconRepository) {
	orderNo := generateOrderNo("NOTIFY-SUB")
	subOrderNo := orderNo + "-SUB1"
	repo.SaveOrderMain(&entity.ReconOrderMain{OrderNo: orderNo, MerchantId: "MCH-SUB"})
	repo.BatchSaveOrderSplitSub([]*entity.ReconOrderSplitSub{
		{OrderNo: orderNo, SubOrderNo: subOrderNo, MerchantId: "MCH-SUB"},
	})

	res := template.ReconNotifyBySub("MCH-SUB", subOrderNo, "http://callback.com", entity.NotifyStatusSuccess, "OK")
	printResult(orderNo, res.Success, res.Msg)
}

// 8. Recon Notify By Merchant Order
func demoReconNotifyByMerchantOrder(template *core.EasyReconTemplate, repo repository.ReconRepository) {
	orderNo := generateOrderNo("NOTIFY-MCH")
	mchOrderNo := "MCH-" + orderNo
	repo.SaveOrderMain(&entity.ReconOrderMain{
		OrderNo:         orderNo,
		MerchantId:      "MCH-MAIN",
		MerchantOrderNo: mchOrderNo,
	})

	res := template.ReconNotifyByMerchantOrder("MCH-MAIN", mchOrderNo, "http://callback.com", entity.NotifyStatusSuccess, "OK")
	printResult(orderNo, res.Success, res.Msg)
}

// 9. Refund By Sub
func demoRefundBySub(template *core.EasyReconTemplate, repo repository.ReconRepository) {
	orderNo := generateOrderNo("REF-SUB")
	subOrderNo := orderNo + "-SUB1"
	repo.SaveOrderMain(&entity.ReconOrderMain{OrderNo: orderNo, MerchantId: "MCH-REF"})
	repo.BatchSaveOrderSplitSub([]*entity.ReconOrderSplitSub{
		{OrderNo: orderNo, SubOrderNo: subOrderNo, MerchantId: "MCH-REF", SplitAmount: decimal.NewFromFloat(100)},
	})

	res := template.ReconRefundBySub("MCH-REF", subOrderNo, decimal.NewFromFloat(10), time.Now(), 1)
	printResult(orderNo, res.Success, res.Msg)
}

// 10. Refund By Merchant Order
func demoRefundByMerchantOrder(template *core.EasyReconTemplate, repo repository.ReconRepository) {
	orderNo := generateOrderNo("REF-MCH")
	mchOrderNo := "MCH-" + orderNo
	repo.SaveOrderMain(&entity.ReconOrderMain{
		OrderNo:         orderNo,
		MerchantId:      "MCH-REF",
		MerchantOrderNo: mchOrderNo,
	})

	res := template.ReconRefundByMerchantOrder("MCH-REF", mchOrderNo, decimal.NewFromFloat(20), time.Now(), 1)
	printResult(orderNo, res.Success, res.Msg)
}

// 11. Query
func demoQuery(template *core.EasyReconTemplate) {
	orderNo := generateOrderNo("QUERY")
	// Status
	status := template.GetReconStatus(orderNo)
	fmt.Printf("Order %s status: %d\n", orderNo, status)

	// Summary
	summary := template.GetReconSummary(time.Now().Format("2006-01-02"))
	fmt.Printf("Summary: Total=%d, Amount=%s\n", summary.TotalOrders, summary.TotalAmount)
}

func generateOrderNo(prefix string) string {
	return fmt.Sprintf("ORD-%s-%d-%d", prefix, time.Now().Unix(), rand.Intn(1000))
}

func printResult(orderNo string, success bool, msg string) {
	if success {
		fmt.Printf("✅ Success. Order: %s\n", orderNo)
	} else {
		fmt.Printf("❌ Failed. Order: %s, Msg: %s\n", orderNo, msg)
	}
}
