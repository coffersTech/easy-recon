package entity

import (
	"time"

	"github.com/shopspring/decimal"
)

// ReconOrderMain 对账订单主记录
type ReconOrderMain struct {
	ID               int64           `json:"id" gorm:"primaryKey"`
	OrderNo          string          `json:"orderNo"`
	MerchantId       string          `json:"merchantId"`
	MerchantName     string          `json:"merchantName"`
	MerchantOrderNo  string          `json:"merchantOrderNo"`
	OrderAmount      decimal.Decimal `json:"orderAmount" gorm:"type:decimal(20,2)"`
	ActualAmount     decimal.Decimal `json:"actualAmount" gorm:"type:decimal(20,2)"`
	PlatformIncome   decimal.Decimal `json:"platformIncome" gorm:"type:decimal(20,2)"`
	PayFee           decimal.Decimal `json:"payFee" gorm:"type:decimal(20,2)"`
	SplitTotalAmount decimal.Decimal `json:"splitTotalAmount" gorm:"type:decimal(20,2)"`
	PayStatus        int             `json:"payStatus"`
	SplitStatus      int             `json:"splitStatus"`
	NotifyStatus     int             `json:"notifyStatus"`
	ReconStatus      int             `json:"reconStatus"`
	OrderTime        time.Time       `json:"orderTime"`
	PayTime          time.Time       `json:"payTime"`
	ReconTime        *time.Time      `json:"reconTime"`
	CreateTime       time.Time       `json:"createTime"`
	UpdateTime       time.Time       `json:"updateTime"`
}
