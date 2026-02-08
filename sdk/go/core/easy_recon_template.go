package core

import (
	"github.com/coffersTech/easy-recon/sdk/go/entity"
	"github.com/coffersTech/easy-recon/sdk/go/service"
)

// EasyReconTemplate 对账SDK核心模板类
type EasyReconTemplate struct {
	realtimeReconService *service.RealtimeReconService
	timingReconService   *service.TimingReconService
}

// NewEasyReconTemplate 创建对账SDK核心模板
func NewEasyReconTemplate(realtimeReconService *service.RealtimeReconService, timingReconService *service.TimingReconService) *EasyReconTemplate {
	return &EasyReconTemplate{
		realtimeReconService: realtimeReconService,
		timingReconService:   timingReconService,
	}
}

// DoRealtimeRecon 执行实时对账
func (t *EasyReconTemplate) DoRealtimeRecon(orderMain *entity.ReconOrderMain, splitSubs []*entity.ReconOrderSplitSub) (bool, error) {
	return t.realtimeReconService.DoRealtimeRecon(orderMain, splitSubs)
}

// DoTimingRecon 执行定时对账
func (t *EasyReconTemplate) DoTimingRecon(dateStr string) (bool, error) {
	return t.timingReconService.DoTimingRecon(dateStr)
}
