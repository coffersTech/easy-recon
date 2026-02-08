package entity

import (
	"time"
)

// ReconOrderMain 对账订单主记录
type ReconOrderMain struct {
	ID            int64     `json:"id"`
	OrderNo       string    `json:"orderNo"`
	MerchantId    string    `json:"merchantId"`
	MerchantName  string    `json:"merchantName"`
	OrderAmount   float64   `json:"orderAmount"`
	ActualAmount  float64   `json:"actualAmount"`
	ReconStatus   int       `json:"reconStatus"`
	OrderTime     time.Time `json:"orderTime"`
	PayTime       time.Time `json:"payTime"`
	ReconTime     time.Time `json:"reconTime"`
	CreateTime    time.Time `json:"createTime"`
	UpdateTime    time.Time `json:"updateTime"`
}
