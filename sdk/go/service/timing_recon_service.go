package service

import (
	"easy-recon-sdk/entity"
	"easy-recon-sdk/repository"
	"fmt"
)

// TimingReconService 定时对账服务
type TimingReconService struct {
	reconRepository repository.ReconRepository
	alarmService    *AlarmService
}

// NewTimingReconService 创建定时对账服务
func NewTimingReconService(reconRepository repository.ReconRepository, alarmService *AlarmService) *TimingReconService {
	return &TimingReconService{
		reconRepository: reconRepository,
		alarmService:    alarmService,
	}
}

// DoTimingRecon 执行定时对账
func (s *TimingReconService) DoTimingRecon(dateStr string) (bool, error) {
	var totalProcessed int
	limit := 100
	offset := 0

	for {
		// 查询待核账订单
		pendingOrders, err := s.reconRepository.GetPendingReconOrders(dateStr, offset, limit)
		if err != nil {
			s.alarmService.SendAlarm("查询待核账订单失败: " + err.Error())
			return false, err
		}

		if len(pendingOrders) == 0 {
			break
		}

		// 处理每个待核账订单
		for _, order := range pendingOrders {
			if err := s.processPendingOrder(order); err != nil {
				s.alarmService.SendAlarm(fmt.Sprintf("处理订单 %s 失败: %s", order.OrderNo, err.Error()))
			}
			totalProcessed++
		}

		offset += limit
	}

	s.alarmService.SendAlarm(fmt.Sprintf("定时对账完成，共处理 %d 笔订单", totalProcessed))
	return true, nil
}

// processPendingOrder 处理待核账订单
func (s *TimingReconService) processPendingOrder(order *entity.ReconOrderMain) error {
	// 这里可以添加更复杂的对账逻辑
	// 例如：与第三方支付平台对账、金额校验等

	// 更新对账状态为已对账
	_, err := s.reconRepository.UpdateReconStatus(order.OrderNo, 1) // 1: 已对账
	return err
}
