package entity

import (
	"time"
)

// ReconOrderSplitSub 对账订单分账子记录
type ReconOrderSplitSub struct {
	ID          int64     `json:"id"`
	OrderNo     string    `json:"orderNo"`
	SubOrderNo  string    `json:"subOrderNo"`
	MerchantId  string    `json:"merchantId"`
	SplitAmount float64   `json:"splitAmount"`
	Status      int       `json:"status"`
	CreateTime  time.Time `json:"createTime"`
	UpdateTime  time.Time `json:"updateTime"`
}
