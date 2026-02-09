package main

import (
	"fmt"
	"time"

	"github.com/coffersTech/easy-recon/sdk/go/entity"
	"github.com/coffersTech/easy-recon/sdk/go/repository"
	"github.com/coffersTech/easy-recon/sdk/go/service"
)

// MockReconRepository
type MockReconRepository struct{}

func (m *MockReconRepository) SaveOrderMain(orderMain *entity.ReconOrderMain) (bool, error) {
	fmt.Printf("Saving Order Main: %+v\n", orderMain)
	return true, nil
}

func (m *MockReconRepository) BatchSaveOrderSplitSub(splitSubs []*entity.ReconOrderSplitSub) (bool, error) {
	fmt.Printf("Batch Saving Order Split Subs: %d records\n", len(splitSubs))
	return true, nil
}

func (m *MockReconRepository) UpdateReconStatus(orderNo string, status int) (bool, error) {
	fmt.Printf("Updating Recon Status for %s to %d\n", orderNo, status)
	return true, nil
}

// Implement other interface methods to satisfy repository.ReconRepository
func (m *MockReconRepository) SaveException(exception *entity.ReconException) (bool, error) {
	return true, nil
}
func (m *MockReconRepository) BatchSaveException(exceptions []*entity.ReconException) (bool, error) {
	return true, nil
}
func (m *MockReconRepository) GetOrderMainByOrderNo(orderNo string) (*entity.ReconOrderMain, error) {
	return nil, nil
}
func (m *MockReconRepository) GetOrderSplitSubByOrderNo(orderNo string) ([]*entity.ReconOrderSplitSub, error) {
	return nil, nil
}
func (m *MockReconRepository) GetPendingReconOrders(dateStr string, offset, limit int) ([]*entity.ReconOrderMain, error) {
	return nil, nil
}
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

func main() {
	fmt.Println("--- Starting Go Easy Recon SDK Demo ---")

	// Initialize Repository
	var repo repository.ReconRepository = &MockReconRepository{}

	// Initialize Alarm Service with Log Strategy
	alarmService := service.NewAlarmService(&service.LogAlarmStrategy{})

	// Initialize Realtime Recon Service
	reconService := service.NewRealtimeReconService(repo, alarmService)

	// Create Mock Data
	orderMain := &entity.ReconOrderMain{
		OrderNo:     "ORD-GO-123456",
		OrderAmount: 200.00,
		MerchantId:  "MCH-GO-001",
		OrderTime:   time.Now(),
	}

	splitSubs := []*entity.ReconOrderSplitSub{
		{SubOrderNo: "SUB-GO-001", SplitAmount: 150.00},
		{SubOrderNo: "SUB-GO-002", SplitAmount: 50.00},
	}

	// Execute Recon
	success, err := reconService.DoRealtimeRecon(orderMain, splitSubs)
	if err != nil {
		fmt.Printf("Error executing demo: %v\n", err)
		return
	}

	if success {
		fmt.Println("--- Recon Successful ---")
	} else {
		fmt.Println("--- Recon Failed ---")
	}
}
