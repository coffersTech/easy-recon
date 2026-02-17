package entity

import (
	"time"
)

// ReconNotifyLog 对账通知日志
type ReconNotifyLog struct {
	ID           int64     `json:"id" gorm:"primaryKey"`
	OrderNo      string    `json:"orderNo"`
	SubOrderNo   string    `json:"subOrderNo"`
	MerchantId   string    `json:"merchantId"`
	NotifyUrl    string    `json:"notifyUrl"`
	NotifyStatus int       `json:"notifyStatus"` // 0:失败, 1:成功
	NotifyResult string    `json:"notifyResult"`
	CreateTime   time.Time `json:"createTime"`
	UpdateTime   time.Time `json:"updateTime"`
}
