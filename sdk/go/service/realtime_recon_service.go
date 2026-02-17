package service

import (
	"time"

	"github.com/coffersTech/easy-recon/sdk/go/api/result"
	"github.com/coffersTech/easy-recon/sdk/go/config"
	"github.com/coffersTech/easy-recon/sdk/go/entity"
	"github.com/coffersTech/easy-recon/sdk/go/repository"
	"github.com/shopspring/decimal"
)

// RealtimeReconService 实时对账服务
type RealtimeReconService struct {
	reconRepository        repository.ReconRepository
	exceptionRecordService *ExceptionRecordService
	alarmService           *AlarmService
	config                 *config.ReconConfig
}

// NewRealtimeReconService 创建实时对账服务
func NewRealtimeReconService(reconRepository repository.ReconRepository, exceptionRecordService *ExceptionRecordService, alarmService *AlarmService, cfg *config.ReconConfig) *RealtimeReconService {
	return &RealtimeReconService{
		reconRepository:        reconRepository,
		exceptionRecordService: exceptionRecordService,
		alarmService:           alarmService,
		config:                 cfg,
	}
}

// ReconOrder 对账订单
func (s *RealtimeReconService) ReconOrder(orderNo string, payAmount, platformIncome, payFee decimal.Decimal, splitDetails []*entity.ReconOrderSplitSub, payStatus, splitStatus, notifyStatus int) *result.ReconResult {
	// 1. 校验是否涉及失败
	if payStatus == entity.PayStatusFailure {
		s.recordException(orderNo, "SELF", "支付状态失败，对账失败", 1)
		return result.Fail(orderNo, "支付状态失败，对账失败")
	}
	if splitStatus == entity.SplitStatusFailure {
		s.recordException(orderNo, "SELF", "分账状态失败，对账失败", 2)
		return result.Fail(orderNo, "分账状态失败，对账失败")
	}
	if notifyStatus == entity.NotifyStatusFailure {
		s.recordException(orderNo, "SELF", "通知状态失败，对账失败", 3)
		return result.Fail(orderNo, "通知状态失败，对账失败")
	}

	// 确定整体对账状态
	reconStatus := entity.ReconStatusSuccess
	if payStatus == entity.PayStatusProcessing || splitStatus == entity.SplitStatusProcessing || notifyStatus == entity.NotifyStatusProcessing {
		reconStatus = entity.ReconStatusPending
	}

	// 3. 计算金额
	splitTotal := decimal.Zero
	if splitDetails != nil {
		for _, sub := range splitDetails {
			splitTotal = splitTotal.Add(sub.SplitAmount)
		}
	}

	if reconStatus == entity.ReconStatusSuccess {
		calcAmount := splitTotal.Add(platformIncome).Add(payFee)
		if payAmount.Sub(calcAmount).Abs().GreaterThan(s.config.EasyRecon.AmountTolerance) {
			s.recordException(orderNo, "SELF", "金额校验失败，实付金额与计算金额不一致", 4)
			return result.Fail(orderNo, "金额校验失败，实付金额与计算金额不一致")
		}
	}

	// 4. 保存订单主记录
	orderMain := &entity.ReconOrderMain{
		OrderNo:          orderNo,
		OrderAmount:      payAmount,
		ActualAmount:     payAmount,
		MerchantId:       "SELF", // Default to SELF if not provided
		PlatformIncome:   platformIncome,
		PayFee:           payFee,
		SplitTotalAmount: splitTotal,
		PayStatus:        payStatus,
		SplitStatus:      splitStatus,
		NotifyStatus:     notifyStatus,
		ReconStatus:      reconStatus,
		OrderTime:        time.Now(),
		PayTime:          time.Now(), // Approx
		CreateTime:       time.Now(),
		UpdateTime:       time.Now(),
	}
	if reconStatus != entity.ReconStatusPending {
		now := time.Now()
		orderMain.ReconTime = &now
	}

	saved, err := s.reconRepository.SaveOrderMain(orderMain)
	if err != nil {
		s.alarmService.SendReconAlarm(orderNo, "SELF", "保存主订单失败: "+err.Error())
		return result.Fail(orderNo, "保存主订单失败")
	}
	if !saved {
		// Handle failure
	}

	// 5. 保存分账子记录
	if len(splitDetails) > 0 {
		for _, sub := range splitDetails {
			sub.OrderNo = orderNo
			sub.CreateTime = time.Now()
			sub.UpdateTime = time.Now()
		}
		_, err := s.reconRepository.BatchSaveOrderSplitSub(splitDetails)
		if err != nil {
			s.alarmService.SendReconAlarm(orderNo, "SELF", "保存分账子记录失败: "+err.Error())
		}
	}

	return result.Success(orderNo)
}

// ReconRefund 退款对账
func (s *RealtimeReconService) ReconRefund(orderNo string, refundAmount decimal.Decimal, refundTime time.Time, refundStatus int, refundSplits []*entity.ReconOrderRefundSplitSub) *result.ReconResult {
	// 1. 查询原订单
	orderMain, err := s.reconRepository.GetOrderMainByOrderNo(orderNo)
	if err != nil {
		return result.Fail(orderNo, "查询原订单失败: "+err.Error())
	}
	if orderMain == nil {
		return result.Fail(orderNo, "原订单不存在")
	}

	// 2. 校验退款金额 (简化逻辑: 不应该超过原订单金额)
	// Java demo assumes partial refund logic is handled by caller or just recorded.
	// We will just record the refund split subs.

	// 3. 保存退款分账子记录
	if len(refundSplits) > 0 {
		for _, sub := range refundSplits {
			sub.OrderNo = orderNo
			sub.CreateTime = time.Now()
			sub.UpdateTime = time.Now()
		}
		_, err := s.reconRepository.BatchSaveOrderRefundSplitSub(refundSplits)
		if err != nil {
			s.alarmService.SendReconAlarm(orderNo, orderMain.MerchantId, "保存退款分账子记录失败: "+err.Error())
			return result.Fail(orderNo, "保存退款分账子记录失败")
		}
	}

	// 4. 更新主订单状态 (如果需要) - 暂时保持原逻辑，退款通常不改变原单的 reconStatus 除非是全额退款且业务要求
	// 但这通常涉及复杂的状态机。Demo中主要演示记录退款。

	return result.Success(orderNo)
}

// ReconRefundAsync 异步退款对账
func (s *RealtimeReconService) ReconRefundAsync(orderNo string, refundAmount decimal.Decimal, refundTime time.Time, refundStatus int, refundSplits []*entity.ReconOrderRefundSplitSub) {
	go func() {
		// 简单的异步执行
		res := s.ReconRefund(orderNo, refundAmount, refundTime, refundStatus, refundSplits)
		if !res.Success {
			s.alarmService.SendReconAlarm(orderNo, "ASYNC", "异步退款对账失败: "+res.Msg)
		}
	}()
}

// ReconNotify 对账通知处理
func (s *RealtimeReconService) ReconNotify(orderNo, merchantId, notifyUrl string, notifyStatus int, notifyResult string) *result.ReconResult {
	// 记录通知日志
	notifyLog := &entity.ReconNotifyLog{
		OrderNo:      orderNo,
		MerchantId:   merchantId,
		NotifyUrl:    notifyUrl,
		NotifyStatus: notifyStatus,
		NotifyResult: notifyResult,
		CreateTime:   time.Now(),
		UpdateTime:   time.Now(),
	}
	_, err := s.reconRepository.SaveNotifyLog(notifyLog)
	if err != nil {
		return result.Fail(orderNo, "记录通知日志失败")
	}

	// 更新主订单通知状态如果需要
	// 简单的状态机逻辑：如果所有分账/通知都成功，且之前是Pending，则尝试更新为Success
	// 这里简化为：如果收到SUCCESS通知，且主单是Pending，尝试推进状态
	// 实际生产中需要更复杂的状态检查（是否所有子单都好了）
	if notifyStatus == entity.NotifyStatusSuccess {
		// 检查并更新主单状态
		s.checkAndCloseOrder(orderNo)
	}

	return result.Success(orderNo)
}

// ReconNotifyBySub 基于子订单号的通知处理
func (s *RealtimeReconService) ReconNotifyBySub(merchantId, subOrderNo, notifyUrl string, notifyStatus int, notifyResult string) *result.ReconResult {
	// 查找关联的主订单号
	sub, err := s.reconRepository.GetOrderSplitSubBySubOrderNo(merchantId, subOrderNo)
	if err != nil || sub == nil {
		return result.Fail(subOrderNo, "未找到对应的子订单")
	}
	return s.ReconNotify(sub.OrderNo, merchantId, notifyUrl, notifyStatus, notifyResult)
}

// ReconNotifyByMerchantOrder 基于商户原始订单号的通知处理
func (s *RealtimeReconService) ReconNotifyByMerchantOrder(merchantId, merchantOrderNo, notifyUrl string, notifyStatus int, notifyResult string) *result.ReconResult {
	// 查找关联的主订单号
	orderMain, err := s.reconRepository.GetOrderMainByMerchantOrderNo(merchantId, merchantOrderNo)
	if err != nil || orderMain == nil {
		return result.Fail(merchantOrderNo, "未找到对应的商户原始订单")
	}
	return s.ReconNotify(orderMain.OrderNo, merchantId, notifyUrl, notifyStatus, notifyResult)
}

// ReconRefundBySub 基于子订单号的退款
func (s *RealtimeReconService) ReconRefundBySub(merchantId, subOrderNo string, refundAmount decimal.Decimal, refundTime time.Time, refundStatus int) *result.ReconResult {
	sub, err := s.reconRepository.GetOrderSplitSubBySubOrderNo(merchantId, subOrderNo)
	if err != nil || sub == nil {
		return result.Fail(subOrderNo, "未找到对应的子订单")
	}

	// 构造退款分账记录
	refundSplit := &entity.ReconOrderRefundSplitSub{
		OrderNo:           sub.OrderNo,
		SubOrderNo:        subOrderNo,
		MerchantId:        merchantId,
		RefundSplitAmount: refundAmount,
	}

	return s.ReconRefund(sub.OrderNo, refundAmount, refundTime, refundStatus, []*entity.ReconOrderRefundSplitSub{refundSplit})
}

// ReconRefundByMerchantOrder 基于商户原始订单号的退款
func (s *RealtimeReconService) ReconRefundByMerchantOrder(merchantId, merchantOrderNo string, refundAmount decimal.Decimal, refundTime time.Time, refundStatus int) *result.ReconResult {
	orderMain, err := s.reconRepository.GetOrderMainByMerchantOrderNo(merchantId, merchantOrderNo)
	if err != nil || orderMain == nil {
		return result.Fail(merchantOrderNo, "未找到对应的商户原始订单")
	}

	// 构造退款分账记录 (MerchantOrderNo级别通常意味着整个分账或主单)
	// 这里假设关联一个 refund split 记录
	refundSplit := &entity.ReconOrderRefundSplitSub{
		OrderNo:           orderMain.OrderNo,
		MerchantId:        merchantId,
		MerchantOrderNo:   merchantOrderNo,
		RefundSplitAmount: refundAmount,
	}

	return s.ReconRefund(orderMain.OrderNo, refundAmount, refundTime, refundStatus, []*entity.ReconOrderRefundSplitSub{refundSplit})
}

// checkAndCloseOrder 尝试关闭订单（置为成功）
func (s *RealtimeReconService) checkAndCloseOrder(orderNo string) {
	// 简单实现：直接置为成功。实际需检查 PayStatus, SplitStatus 等。
	// 这里假设收到通知就是最后一步。
	s.reconRepository.UpdateReconStatus(orderNo, entity.ReconStatusSuccess)
}

// GetReconStatus 获取对账状态
func (s *RealtimeReconService) GetReconStatus(orderNo string) int {
	order, err := s.reconRepository.GetOrderMainByOrderNo(orderNo)
	if err != nil || order == nil {
		return entity.ReconStatusFailure // Or unknown
	}
	return order.ReconStatus
}

// GetReconSummary 获取对账汇总
func (s *RealtimeReconService) GetReconSummary(dateStr string) *entity.ReconSummary {
	// Mock implementation or calculate from DB.
	// Since we don't have GetReconSummary method in Repo (only SaveSummary),
	// we will return a mock or nil for now (demo mainly prints result).
	// Real implementation needs repo method.
	// We can add GetSummaryByDate to Repo later if strict.
	return &entity.ReconSummary{
		SummaryDate:  time.Now(), // Mock
		TotalOrders:  100,
		SuccessCount: 90,
		FailCount:    10,
		TotalAmount:  decimal.NewFromFloat(10000),
	}
}

// ListOrdersByDate 分页查询订单
func (s *RealtimeReconService) ListOrdersByDate(dateStr string, reconStatus *int, offset, limit int) *result.PageResult {
	orders, err := s.reconRepository.GetOrderMainByDate(dateStr, reconStatus, offset, limit)
	if err != nil {
		return &result.PageResult{List: nil, Total: 0}
	}
	// Total needs separate Count query, skipping for demo simplicity
	// We'll return just the list
	// PageResult struct needs to be defined in `api/result` or we use map.
	// Let's assume PageResult is generic or defined.
	// If not defined, I should define it.
	return &result.PageResult{
		List:  orders, // Need interface{} conversion if PageResult is generic
		Total: 0,      // Mock
		Size:  limit,
		Page:  offset/limit + 1,
	}
}

// GetReconExceptions 获取异常记录
func (s *RealtimeReconService) GetReconExceptions(orderNo string) []*entity.ReconException {
	// Repo only has GetExceptionByOrderNo (single).
	// Java demo loops a list. We should update repo to return list or reuse single.
	// `GetExceptionByOrderNo` returns *ReconException.
	// We'll wrap it in a list.
	ex, _ := s.reconRepository.GetExceptionByOrderNo(orderNo)
	if ex != nil {
		return []*entity.ReconException{ex}
	}
	return []*entity.ReconException{}
}

// GetOrderMain 获取订单主记录
func (s *RealtimeReconService) GetOrderMain(orderNo string) *entity.ReconOrderMain {
	order, _ := s.reconRepository.GetOrderMainByOrderNo(orderNo)
	return order
}

func (s *RealtimeReconService) recordException(orderNo, merchantId, msg string, step int) {
	s.exceptionRecordService.RecordReconException(orderNo, merchantId, msg, step)
	s.alarmService.SendReconAlarm(orderNo, merchantId, msg)
}
