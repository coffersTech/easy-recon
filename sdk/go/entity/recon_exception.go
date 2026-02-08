package entity

import (
	"time"
)

// ReconException 对账异常记录
type ReconException struct {
	ID            int64     `json:"id"`
	OrderNo       string    `json:"orderNo"`
	MerchantId    string    `json:"merchantId"`
	ExceptionType int       `json:"exceptionType"`
	ExceptionMsg  string    `json:"exceptionMsg"`
	ExceptionStep int       `json:"exceptionStep"`
	CreateTime    time.Time `json:"createTime"`
	UpdateTime    time.Time `json:"updateTime"`
}
