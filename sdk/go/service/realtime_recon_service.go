package service

import (
	"github.com/coffersTech/easy-recon/sdk/go/entity"
	"github.com/coffersTech/easy-recon/sdk/go/repository"
)

// RealtimeReconService 实时对账服务
type RealtimeReconService struct {
	reconRepository repository.ReconRepository
	alarmService    *AlarmService
}

// NewRealtimeReconService 创建实时对账服务
func NewRealtimeReconService(reconRepository repository.ReconRepository, alarmService *AlarmService) *RealtimeReconService {
	return &RealtimeReconService{
		reconRepository: reconRepository,
		alarmService:    alarmService,
	}
}

// DoRealtimeRecon 执行实时对账
func (s *RealtimeReconService) DoRealtimeRecon(orderMain *entity.ReconOrderMain, splitSubs []*entity.ReconOrderSplitSub) (bool, error) {
	// 1. 保存订单主记录
	mainSaved, err := s.reconRepository.SaveOrderMain(orderMain)
	if err != nil {
		s.alarmService.SendAlarm("保存订单主记录失败: " + err.Error())
		return false, err
	}
	if !mainSaved {
		return false, nil
	}

	// 2. 批量保存分账子记录
	if len(splitSubs) > 0 {
		subSaved, err := s.reconRepository.BatchSaveOrderSplitSub(splitSubs)
		if err != nil {
			s.alarmService.SendAlarm("批量保存分账子记录失败: " + err.Error())
			return false, err
		}
		if !subSaved {
			return false, nil
		}
	}

	// 3. 更新对账状态为已对账
	statusUpdated, err := s.reconRepository.UpdateReconStatus(orderMain.OrderNo, 1) // 1: 已对账
	if err != nil {
		s.alarmService.SendAlarm("更新对账状态失败: " + err.Error())
		return false, err
	}
	if !statusUpdated {
		return false, nil
	}

	return true, nil
}
