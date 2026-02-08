package repository

import (
	"github.com/coffersTech/easy-recon/sdk/go/entity"
)

// ReconRepository 对账存储库接口
type ReconRepository interface {
	// 保存对账订单主记录
	SaveOrderMain(orderMain *entity.ReconOrderMain) (bool, error)

	// 批量保存分账子记录
	BatchSaveOrderSplitSub(splitSubs []*entity.ReconOrderSplitSub) (bool, error)

	// 保存异常记录
	SaveException(exception *entity.ReconException) (bool, error)

	// 批量保存异常记录
	BatchSaveException(exceptions []*entity.ReconException) (bool, error)

	// 根据订单号查询对账订单主记录
	GetOrderMainByOrderNo(orderNo string) (*entity.ReconOrderMain, error)

	// 根据订单号查询分账子记录
	GetOrderSplitSubByOrderNo(orderNo string) ([]*entity.ReconOrderSplitSub, error)

	// 查询指定日期的待核账订单（分页）
	GetPendingReconOrders(dateStr string, offset, limit int) ([]*entity.ReconOrderMain, error)

	// 更新对账状态
	UpdateReconStatus(orderNo string, reconStatus int) (bool, error)

	// 根据商户ID查询对账订单主记录（分页）
	GetOrderMainByMerchantId(merchantId, startDate, endDate string, reconStatus *int, offset, limit int) ([]*entity.ReconOrderMain, error)

	// 根据日期查询对账订单主记录（分页）
	GetOrderMainByDate(dateStr string, reconStatus *int, offset, limit int) ([]*entity.ReconOrderMain, error)

	// 查询对账异常记录（分页）
	GetExceptionRecords(merchantId, startDate, endDate string, exceptionStep *int, offset, limit int) ([]*entity.ReconException, error)

	// 根据订单号查询对账异常记录
	GetExceptionByOrderNo(orderNo string) (*entity.ReconException, error)
}
