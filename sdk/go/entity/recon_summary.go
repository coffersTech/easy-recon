package entity

import (
	"time"

	"github.com/shopspring/decimal"
)

// ReconSummary 对账汇总统计实体
type ReconSummary struct {
	SummaryDate  time.Time       `json:"summaryDate" gorm:"type:date"`
	TotalOrders  int             `json:"totalOrders"`
	SuccessCount int             `json:"successCount"`
	FailCount    int             `json:"failCount"`
	InitCount    int             `json:"initCount"`
	TotalAmount  decimal.Decimal `json:"totalAmount" gorm:"type:decimal(20,2)"`
	CreateTime   time.Time       `json:"createTime"`
	UpdateTime   time.Time       `json:"updateTime"`
}
