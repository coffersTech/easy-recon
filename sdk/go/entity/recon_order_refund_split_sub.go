package entity

import (
	"time"

	"github.com/shopspring/decimal"
)

// ReconOrderRefundSplitSub 对账订单退款分账子记录
type ReconOrderRefundSplitSub struct {
	ID                int64           `gorm:"primaryKey;autoIncrement;comment:主键 ID"`
	OrderNo           string          `gorm:"size:64;not null;comment:订单号"`
	SubOrderNo        string          `gorm:"size:64;comment:子订单号"`
	MerchantId        string          `gorm:"size:64;not null;comment:商户 ID"`
	MerchantOrderNo   string          `gorm:"size:64;comment:商户原始订单号"`
	RefundSplitAmount decimal.Decimal `gorm:"type:decimal(18,2);not null;comment:退款分账金额"`
	CreateTime        time.Time       `gorm:"autoCreateTime;comment:创建时间"`
	UpdateTime        time.Time       `gorm:"autoUpdateTime;comment:更新时间"`
}
