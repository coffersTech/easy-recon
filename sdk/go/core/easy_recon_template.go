package core

import (
	"time"

	"github.com/coffersTech/easy-recon/sdk/go/api/result"
	"github.com/coffersTech/easy-recon/sdk/go/entity"
	"github.com/coffersTech/easy-recon/sdk/go/service"
	"github.com/shopspring/decimal"
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

// ReconOrder 执行实时对账
func (t *EasyReconTemplate) ReconOrder(orderNo string, payAmount, platformIncome, payFee decimal.Decimal, splitDetails []*entity.ReconOrderSplitSub, payStatus, splitStatus, notifyStatus int) *result.ReconResult {
	return t.realtimeReconService.ReconOrder(orderNo, payAmount, platformIncome, payFee, splitDetails, payStatus, splitStatus, notifyStatus)
}

// DoTimingRecon 执行定时对账
func (t *EasyReconTemplate) DoTimingRecon(dateStr string) (bool, error) {
	return t.timingReconService.DoTimingRecon(dateStr)
}

// ReconRefund 执行退款对账
func (t *EasyReconTemplate) ReconRefund(orderNo string, refundAmount decimal.Decimal, refundTime time.Time, refundStatus int, refundSplits []*entity.ReconOrderRefundSplitSub) *result.ReconResult {
	return t.realtimeReconService.ReconRefund(orderNo, refundAmount, refundTime, refundStatus, refundSplits)
}

// ReconRefundAsync 执行异步退款对账
func (t *EasyReconTemplate) ReconRefundAsync(orderNo string, refundAmount decimal.Decimal, refundTime time.Time, refundStatus int, refundSplits []*entity.ReconOrderRefundSplitSub) {
	t.realtimeReconService.ReconRefundAsync(orderNo, refundAmount, refundTime, refundStatus, refundSplits)
}

// ReconNotify 对账通知处理
func (t *EasyReconTemplate) ReconNotify(orderNo, merchantId, notifyUrl string, notifyStatus int, notifyResult string) *result.ReconResult {
	return t.realtimeReconService.ReconNotify(orderNo, merchantId, notifyUrl, notifyStatus, notifyResult)
}

// ReconNotifyBySub 基于子订单号的通知处理
func (t *EasyReconTemplate) ReconNotifyBySub(merchantId, subOrderNo, notifyUrl string, notifyStatus int, notifyResult string) *result.ReconResult {
	return t.realtimeReconService.ReconNotifyBySub(merchantId, subOrderNo, notifyUrl, notifyStatus, notifyResult)
}

// ReconNotifyByMerchantOrder 基于商户原始订单号的通知处理
func (t *EasyReconTemplate) ReconNotifyByMerchantOrder(merchantId, merchantOrderNo, notifyUrl string, notifyStatus int, notifyResult string) *result.ReconResult {
	return t.realtimeReconService.ReconNotifyByMerchantOrder(merchantId, merchantOrderNo, notifyUrl, notifyStatus, notifyResult)
}

// ReconRefundBySub 基于子订单号的退款
func (t *EasyReconTemplate) ReconRefundBySub(merchantId, subOrderNo string, refundAmount decimal.Decimal, refundTime time.Time, refundStatus int) *result.ReconResult {
	return t.realtimeReconService.ReconRefundBySub(merchantId, subOrderNo, refundAmount, refundTime, refundStatus)
}

// ReconRefundByMerchantOrder 基于商户原始订单号的退款
func (t *EasyReconTemplate) ReconRefundByMerchantOrder(merchantId, merchantOrderNo string, refundAmount decimal.Decimal, refundTime time.Time, refundStatus int) *result.ReconResult {
	return t.realtimeReconService.ReconRefundByMerchantOrder(merchantId, merchantOrderNo, refundAmount, refundTime, refundStatus)
}

// GetReconStatus 获取对账状态
func (t *EasyReconTemplate) GetReconStatus(orderNo string) int {
	return t.realtimeReconService.GetReconStatus(orderNo)
}

// GetReconSummary 获取对账汇总
func (t *EasyReconTemplate) GetReconSummary(dateStr string) *entity.ReconSummary {
	return t.realtimeReconService.GetReconSummary(dateStr)
}

// ListOrdersByDate 分页查询订单
func (t *EasyReconTemplate) ListOrdersByDate(dateStr string, reconStatus *int, offset, limit int) *result.PageResult {
	return t.realtimeReconService.ListOrdersByDate(dateStr, reconStatus, offset, limit)
}

// GetReconExceptions 获取异常记录
func (t *EasyReconTemplate) GetReconExceptions(orderNo string) []*entity.ReconException {
	return t.realtimeReconService.GetReconExceptions(orderNo)
}

// GetOrderMain 获取订单主记录
func (t *EasyReconTemplate) GetOrderMain(orderNo string) *entity.ReconOrderMain {
	return t.realtimeReconService.GetOrderMain(orderNo)
}
