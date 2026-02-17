package entity

import (
	"time"

	"github.com/shopspring/decimal"
)

// ReconOrderSplitSub 对账订单分账子记录
type ReconOrderSplitSub struct {
	ID           int64           `json:"id" gorm:"primaryKey"`
	OrderNo      string          `json:"orderNo"`
	SubOrderNo   string          `json:"subOrderNo"`
	MerchantId   string          `json:"merchantId"`
	SplitAmount  decimal.Decimal `json:"splitAmount" gorm:"type:decimal(20,2)"`
	Status       int             `json:"status"`
	NotifyStatus int             `json:"notifyStatus"`
	NotifyResult string          `json:"notifyResult"`
	CreateTime   time.Time       `json:"createTime"`
	UpdateTime   time.Time       `json:"updateTime"`
}
