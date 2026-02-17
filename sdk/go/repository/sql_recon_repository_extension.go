package repository

import (
	"github.com/coffersTech/easy-recon/sdk/go/entity"
)

// SaveSummary 保存对账汇总统计
func (r *SQLReconRepository) SaveSummary(summary *entity.ReconSummary) (bool, error) {
	sqlStmt := r.rebind("INSERT INTO easy_recon_summary " +
		"(summary_date, total_orders, success_count, fail_count, init_count, total_amount, " +
		"create_time, update_time) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?)")

	_, err := r.db.Exec(sqlStmt,
		summary.SummaryDate,
		summary.TotalOrders,
		summary.SuccessCount,
		summary.FailCount,
		summary.InitCount,
		summary.TotalAmount,
		summary.CreateTime,
		summary.UpdateTime,
	)

	if err != nil {
		return false, err
	}

	return true, nil
}

// SaveNotifyLog 保存对账通知日志
func (r *SQLReconRepository) SaveNotifyLog(notifyLog *entity.ReconNotifyLog) (bool, error) {
	sqlStmt := r.rebind("INSERT INTO easy_recon_notify_log " +
		"(order_no, sub_order_no, merchant_id, notify_url, notify_status, notify_result, " +
		"create_time, update_time) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?)")

	_, err := r.db.Exec(sqlStmt,
		notifyLog.OrderNo,
		notifyLog.SubOrderNo,
		notifyLog.MerchantId,
		notifyLog.NotifyUrl,
		notifyLog.NotifyStatus,
		notifyLog.NotifyResult,
		notifyLog.CreateTime,
		notifyLog.UpdateTime,
	)

	if err != nil {
		return false, err
	}

	return true, nil
}
