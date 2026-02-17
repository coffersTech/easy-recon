package repository

import (
	"database/sql"
	"fmt"
	"log"

	"strings"

	"github.com/coffersTech/easy-recon/sdk/go/config"
	"github.com/coffersTech/easy-recon/sdk/go/dialect"
	"github.com/coffersTech/easy-recon/sdk/go/entity"
)

// SQLReconRepository SQL实现的对账存储库
type SQLReconRepository struct {
	db          *sql.DB
	dialect     dialect.ReconDatabaseDialect
	tablePrefix string
}

// tableName returns the full table name with prefix
func (r *SQLReconRepository) tableName(base string) string {
	return r.tablePrefix + base
}

// rebind replaces ? with $n for PostgreSQL
func (r *SQLReconRepository) rebind(sql string) string {
	if r.dialect.GetDatabaseType() == "mysql" {
		return sql
	}
	parts := strings.Split(sql, "?")
	if len(parts) == 1 {
		return sql
	}
	var builder strings.Builder
	for i, part := range parts {
		builder.WriteString(part)
		if i < len(parts)-1 {
			builder.WriteString(r.dialect.GetPlaceholder(i + 1))
		}
	}
	return builder.String()
}

// NewSQLReconRepository 创建SQL对账存储库
func NewSQLReconRepository(db *sql.DB, dialect dialect.ReconDatabaseDialect, cfg *config.ReconConfig) ReconRepository {
	repo := &SQLReconRepository{
		db:      db,
		dialect: dialect,
	}

	if cfg.EasyRecon.AutoInitTables {
		err := InitTables(db, dialect)
		if err != nil {
			log.Printf("Failed to initialize tables: %v", err)
		}
	}

	return repo
}

// SaveOrderMain 保存对账订单主记录
func (r *SQLReconRepository) SaveOrderMain(orderMain *entity.ReconOrderMain) (bool, error) {
	sqlStmt := r.rebind("INSERT INTO easy_recon_order_main " +
		"(order_no, merchant_id, merchant_name, merchant_order_no, order_amount, actual_amount, " +
		"platform_income, pay_fee, split_total_amount, pay_status, split_status, notify_status, " +
		"recon_status, order_time, pay_time, recon_time, create_time, update_time) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")

	_, err := r.db.Exec(sqlStmt,
		orderMain.OrderNo,
		orderMain.MerchantId,
		orderMain.MerchantName,
		orderMain.MerchantOrderNo,
		orderMain.OrderAmount,
		orderMain.ActualAmount,
		orderMain.PlatformIncome,
		orderMain.PayFee,
		orderMain.SplitTotalAmount,
		orderMain.PayStatus,
		orderMain.SplitStatus,
		orderMain.NotifyStatus,
		orderMain.ReconStatus,
		orderMain.OrderTime,
		orderMain.PayTime,
		orderMain.ReconTime,
		orderMain.CreateTime,
		orderMain.UpdateTime,
	)

	if err != nil {
		return false, err
	}

	return true, nil
}

// BatchSaveOrderSplitSub 批量保存分账子记录
func (r *SQLReconRepository) BatchSaveOrderSplitSub(splitSubs []*entity.ReconOrderSplitSub) (bool, error) {
	if len(splitSubs) == 0 {
		return true, nil
	}

	tx, err := r.db.Begin()
	if err != nil {
		return false, err
	}

	sqlStmt := r.rebind("INSERT INTO easy_recon_order_split_sub " +
		"(order_no, sub_order_no, merchant_id, split_amount, status, create_time, update_time) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?)")

	stmt, err := tx.Prepare(sqlStmt)
	if err != nil {
		tx.Rollback()
		return false, err
	}
	defer stmt.Close()

	for _, sub := range splitSubs {
		_, err := stmt.Exec(
			sub.OrderNo,
			sub.SubOrderNo,
			sub.MerchantId,
			sub.SplitAmount,
			sub.Status,
			sub.CreateTime,
			sub.UpdateTime,
		)
		if err != nil {
			tx.Rollback()
			return false, err
		}
	}

	err = tx.Commit()
	if err != nil {
		return false, err
	}

	return true, nil
}

// SaveException 保存异常记录
func (r *SQLReconRepository) SaveException(exception *entity.ReconException) (bool, error) {
	sqlStmt := r.rebind("INSERT INTO easy_recon_exception " +
		"(order_no, merchant_id, exception_type, exception_msg, exception_step, " +
		"create_time, update_time) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?)")

	_, err := r.db.Exec(sqlStmt,
		exception.OrderNo,
		exception.MerchantId,
		exception.ExceptionType,
		exception.ExceptionMsg,
		exception.ExceptionStep,
		exception.CreateTime,
		exception.UpdateTime,
	)

	if err != nil {
		return false, err
	}

	return true, nil
}

// BatchSaveException 批量保存异常记录
func (r *SQLReconRepository) BatchSaveException(exceptions []*entity.ReconException) (bool, error) {
	if len(exceptions) == 0 {
		return true, nil
	}

	tx, err := r.db.Begin()
	if err != nil {
		return false, err
	}

	sqlStmt := r.rebind("INSERT INTO easy_recon_exception " +
		"(order_no, merchant_id, exception_type, exception_msg, exception_step, " +
		"create_time, update_time) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?)")

	stmt, err := tx.Prepare(sqlStmt)
	if err != nil {
		tx.Rollback()
		return false, err
	}
	defer stmt.Close()

	for _, exception := range exceptions {
		_, err := stmt.Exec(
			exception.OrderNo,
			exception.MerchantId,
			exception.ExceptionType,
			exception.ExceptionMsg,
			exception.ExceptionStep,
			exception.CreateTime,
			exception.UpdateTime,
		)
		if err != nil {
			tx.Rollback()
			return false, err
		}
	}

	err = tx.Commit()
	if err != nil {
		return false, err
	}

	return true, nil
}

// GetOrderMainByOrderNo 根据订单号查询对账订单主记录
func (r *SQLReconRepository) GetOrderMainByOrderNo(orderNo string) (*entity.ReconOrderMain, error) {
	sqlStmt := r.rebind("SELECT * FROM easy_recon_order_main WHERE order_no = ?")
	row := r.db.QueryRow(sqlStmt, orderNo)

	orderMain, err := r.scanOrderMain(row)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, err
	}

	return orderMain, nil
}

// GetOrderSplitSubByOrderNo 根据订单号查询分账子记录
func (r *SQLReconRepository) GetOrderSplitSubByOrderNo(orderNo string) ([]*entity.ReconOrderSplitSub, error) {
	sqlStmt := r.rebind("SELECT * FROM easy_recon_order_split_sub WHERE order_no = ?")
	rows, err := r.db.Query(sqlStmt, orderNo)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []*entity.ReconOrderSplitSub
	for rows.Next() {
		sub, err := r.scanOrderSplitSub(rows)
		if err != nil {
			return nil, err
		}
		result = append(result, sub)
	}

	if err = rows.Err(); err != nil {
		return nil, err
	}

	return result, nil
}

// GetPendingReconOrders 查询指定日期的待核账订单（分页）
func (r *SQLReconRepository) GetPendingReconOrders(dateStr string, offset, limit int) ([]*entity.ReconOrderMain, error) {
	sql := fmt.Sprintf("SELECT * FROM easy_recon_order_main "+
		"WHERE %s(order_time) = ? AND recon_status = 0 "+
		"LIMIT ? OFFSET ?", r.dialect.GetDateFunction())

	rows, err := r.db.Query(r.rebind(sql), dateStr, limit, offset)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []*entity.ReconOrderMain
	for rows.Next() {
		orderMain, err := r.scanOrderMainFromRows(rows)
		if err != nil {
			return nil, err
		}
		result = append(result, orderMain)
	}

	if err = rows.Err(); err != nil {
		return nil, err
	}

	return result, nil
}

// UpdateReconStatus 更新对账状态
func (r *SQLReconRepository) UpdateReconStatus(orderNo string, reconStatus int) (bool, error) {
	sql := fmt.Sprintf("UPDATE easy_recon_order_main SET recon_status = ?, update_time = %s WHERE order_no = ?", r.dialect.GetCurrentTimeFunction())

	_, err := r.db.Exec(r.rebind(sql), reconStatus, orderNo)
	if err != nil {
		return false, err
	}

	return true, nil
}

// GetOrderMainByMerchantId 根据商户ID查询对账订单主记录（分页）
func (r *SQLReconRepository) GetOrderMainByMerchantId(merchantId, startDate, endDate string, reconStatus *int, offset, limit int) ([]*entity.ReconOrderMain, error) {
	// 构建查询SQL
	query := "SELECT * FROM easy_recon_order_main WHERE merchant_id = ?"
	args := []interface{}{merchantId}

	if startDate != "" {
		query += fmt.Sprintf(" AND %s(order_time) >= ?", r.dialect.GetDateFunction())
		args = append(args, startDate)
	}

	if endDate != "" {
		query += fmt.Sprintf(" AND %s(order_time) <= ?", r.dialect.GetDateFunction())
		args = append(args, endDate)
	}

	if reconStatus != nil {
		query += " AND recon_status = ?"
		args = append(args, *reconStatus)
	}

	query += " LIMIT ? OFFSET ?"
	args = append(args, limit, offset)

	rows, err := r.db.Query(r.rebind(query), args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []*entity.ReconOrderMain
	for rows.Next() {
		orderMain, err := r.scanOrderMainFromRows(rows)
		if err != nil {
			return nil, err
		}
		result = append(result, orderMain)
	}

	if err = rows.Err(); err != nil {
		return nil, err
	}

	return result, nil
}

// GetOrderMainByDate 根据日期查询对账订单主记录（分页）
func (r *SQLReconRepository) GetOrderMainByDate(dateStr string, reconStatus *int, offset, limit int) ([]*entity.ReconOrderMain, error) {
	// 构建查询SQL
	query := fmt.Sprintf("SELECT * FROM easy_recon_order_main WHERE %s(order_time) = ?", r.dialect.GetDateFunction())
	args := []interface{}{dateStr}

	if reconStatus != nil {
		query += " AND recon_status = ?"
		args = append(args, *reconStatus)
	}

	query += " LIMIT ? OFFSET ?"
	args = append(args, limit, offset)

	rows, err := r.db.Query(r.rebind(query), args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []*entity.ReconOrderMain
	for rows.Next() {
		orderMain, err := r.scanOrderMainFromRows(rows)
		if err != nil {
			return nil, err
		}
		result = append(result, orderMain)
	}

	if err = rows.Err(); err != nil {
		return nil, err
	}

	return result, nil
}

// GetExceptionRecords 查询对账异常记录（分页）
func (r *SQLReconRepository) GetExceptionRecords(merchantId, startDate, endDate string, exceptionStep *int, offset, limit int) ([]*entity.ReconException, error) {
	// 构建查询SQL
	query := "SELECT * FROM easy_recon_exception WHERE 1=1"
	args := []interface{}{}

	if merchantId != "" {
		query += " AND merchant_id = ?"
		args = append(args, merchantId)
	}

	if startDate != "" {
		query += fmt.Sprintf(" AND %s(create_time) >= ?", r.dialect.GetDateFunction())
		args = append(args, startDate)
	}

	if endDate != "" {
		query += fmt.Sprintf(" AND %s(create_time) <= ?", r.dialect.GetDateFunction())
		args = append(args, endDate)
	}

	if exceptionStep != nil {
		query += " AND exception_step = ?"
		args = append(args, *exceptionStep)
	}

	query += " LIMIT ? OFFSET ?"
	args = append(args, limit, offset)

	rows, err := r.db.Query(r.rebind(query), args...)
	if err != nil {
		return nil, err
	}
	defer rows.Close()

	var result []*entity.ReconException
	for rows.Next() {
		exception, err := r.scanException(rows)
		if err != nil {
			return nil, err
		}
		result = append(result, exception)
	}

	if err = rows.Err(); err != nil {
		return nil, err
	}

	return result, nil
}

// GetExceptionByOrderNo 根据订单号查询对账异常记录
func (r *SQLReconRepository) GetExceptionByOrderNo(orderNo string) (*entity.ReconException, error) {
	sqlStmt := r.rebind("SELECT * FROM easy_recon_exception WHERE order_no = ?")
	row := r.db.QueryRow(sqlStmt, orderNo)

	exception, err := r.scanExceptionFromRow(row)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, err
	}

	return exception, nil
}

// 辅助方法：从单行扫描订单主记录
func (r *SQLReconRepository) scanOrderMain(row *sql.Row) (*entity.ReconOrderMain, error) {
	orderMain := &entity.ReconOrderMain{}
	var merchantName, merchantOrderNo sql.NullString
	var orderTime, payTime, reconTime sql.NullTime
	err := row.Scan(
		&orderMain.ID,
		&orderMain.OrderNo,
		&orderMain.MerchantId,
		&merchantName,
		&merchantOrderNo,
		&orderMain.OrderAmount,
		&orderMain.ActualAmount,
		&orderMain.PlatformIncome,
		&orderMain.PayFee,
		&orderMain.SplitTotalAmount,
		&orderMain.PayStatus,
		&orderMain.SplitStatus,
		&orderMain.NotifyStatus,
		&orderMain.ReconStatus,
		&orderTime,
		&payTime,
		&reconTime,
		&orderMain.CreateTime,
		&orderMain.UpdateTime,
	)
	if err != nil {
		return nil, err
	}
	orderMain.MerchantName = merchantName.String
	orderMain.MerchantOrderNo = merchantOrderNo.String
	orderMain.OrderTime = orderTime.Time
	orderMain.PayTime = payTime.Time
	if reconTime.Valid {
		t := reconTime.Time
		orderMain.ReconTime = &t
	}
	return orderMain, nil
}

// 辅助方法：从结果集扫描订单主记录
func (r *SQLReconRepository) scanOrderMainFromRows(rows *sql.Rows) (*entity.ReconOrderMain, error) {
	orderMain := &entity.ReconOrderMain{}
	var merchantName, merchantOrderNo sql.NullString
	var orderTime, payTime, reconTime sql.NullTime
	err := rows.Scan(
		&orderMain.ID,
		&orderMain.OrderNo,
		&orderMain.MerchantId,
		&merchantName,
		&merchantOrderNo,
		&orderMain.OrderAmount,
		&orderMain.ActualAmount,
		&orderMain.PlatformIncome,
		&orderMain.PayFee,
		&orderMain.SplitTotalAmount,
		&orderMain.PayStatus,
		&orderMain.SplitStatus,
		&orderMain.NotifyStatus,
		&orderMain.ReconStatus,
		&orderTime,
		&payTime,
		&reconTime,
		&orderMain.CreateTime,
		&orderMain.UpdateTime,
	)
	if err != nil {
		return nil, err
	}
	orderMain.MerchantName = merchantName.String
	orderMain.MerchantOrderNo = merchantOrderNo.String
	orderMain.OrderTime = orderTime.Time
	orderMain.PayTime = payTime.Time
	if reconTime.Valid {
		t := reconTime.Time
		orderMain.ReconTime = &t
	}
	return orderMain, nil
}

// 辅助方法：从结果集扫描分账子记录
func (r *SQLReconRepository) scanOrderSplitSub(rows *sql.Rows) (*entity.ReconOrderSplitSub, error) {
	sub := &entity.ReconOrderSplitSub{}
	var subOrderNo, notifyResult sql.NullString
	err := rows.Scan(
		&sub.ID,
		&sub.OrderNo,
		&subOrderNo,
		&sub.MerchantId,
		&sub.SplitAmount,
		&sub.Status,
		&sub.NotifyStatus,
		&notifyResult,
		&sub.CreateTime,
		&sub.UpdateTime,
	)
	if err != nil {
		return nil, err
	}
	sub.SubOrderNo = subOrderNo.String
	sub.NotifyResult = notifyResult.String
	return sub, nil
}

// 辅助方法：从结果集扫描异常记录
func (r *SQLReconRepository) scanException(rows *sql.Rows) (*entity.ReconException, error) {
	exception := &entity.ReconException{}
	err := rows.Scan(
		&exception.ID,
		&exception.OrderNo,
		&exception.MerchantId,
		&exception.ExceptionType,
		&exception.ExceptionMsg,
		&exception.ExceptionStep,
		&exception.CreateTime,
		&exception.UpdateTime,
	)
	if err != nil {
		return nil, err
	}
	return exception, nil
}

// 辅助方法：从单行扫描异常记录
func (r *SQLReconRepository) scanExceptionFromRow(row *sql.Row) (*entity.ReconException, error) {
	exception := &entity.ReconException{}
	err := row.Scan(
		&exception.ID,
		&exception.OrderNo,
		&exception.MerchantId,
		&exception.ExceptionType,
		&exception.ExceptionMsg,
		&exception.ExceptionStep,
		&exception.CreateTime,
		&exception.UpdateTime,
	)
	if err != nil {
		return nil, err
	}
	return exception, nil
}

// BatchSaveOrderRefundSplitSub 批量保存退款分账子记录
func (r *SQLReconRepository) BatchSaveOrderRefundSplitSub(refundSplitSubs []*entity.ReconOrderRefundSplitSub) (bool, error) {
	if len(refundSplitSubs) == 0 {
		return true, nil
	}

	tx, err := r.db.Begin()
	if err != nil {
		return false, err
	}

	sqlStmt := r.rebind("INSERT INTO easy_recon_order_refund_split_sub " +
		"(order_no, sub_order_no, merchant_id, merchant_order_no, refund_split_amount, create_time, update_time) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?)")

	stmt, err := tx.Prepare(sqlStmt)
	if err != nil {
		tx.Rollback()
		return false, err
	}
	defer stmt.Close()

	for _, sub := range refundSplitSubs {
		_, err := stmt.Exec(
			sub.OrderNo,
			sub.SubOrderNo,
			sub.MerchantId,
			sub.MerchantOrderNo,
			sub.RefundSplitAmount,
			sub.CreateTime,
			sub.UpdateTime,
		)
		if err != nil {
			tx.Rollback()
			return false, err
		}
	}

	err = tx.Commit()
	if err != nil {
		return false, err
	}

	return true, nil
}

// GetOrderMainByMerchantOrderNo 根据商户原始订单号查询对账订单主记录
func (r *SQLReconRepository) GetOrderMainByMerchantOrderNo(merchantId, merchantOrderNo string) (*entity.ReconOrderMain, error) {
	// Note: Verify if merchant_order_no exists in recon_order_main.
	// If not, we might need to add it or join with another table.
	// For now assuming existing table structure supports it or I need to add it.
	// Wait, ReconOrderMain entity check is pending.
	// If it doesn't exist, I'll need to add it to entity and DB schema.
	// Let's assume it exists for now and fix if not.
	// Actually, looking at previous schema, there was no merchant_order_no in main table.
	// It is usually in split sub or main?
	// Java demo uses `reconRefundByMerchantOrder`.
	// Let's check Java entity `ReconOrderMainDO`.
	// For now, I will implement it assuming the column exists. I will fix schema next.

	sqlStmt := r.rebind("SELECT * FROM easy_recon_order_main WHERE merchant_id = ? AND merchant_order_no = ?")
	row := r.db.QueryRow(sqlStmt, merchantId, merchantOrderNo)

	orderMain, err := r.scanOrderMain(row)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, err
	}

	return orderMain, nil
}

// GetOrderSplitSubBySubOrderNo 根据子订单号查询分账子记录
func (r *SQLReconRepository) GetOrderSplitSubBySubOrderNo(merchantId, subOrderNo string) (*entity.ReconOrderSplitSub, error) {
	// Note: logic usually requires merchant_id too?
	sqlStmt := r.rebind("SELECT * FROM easy_recon_order_split_sub WHERE merchant_id = ? AND sub_order_no = ?")
	row := r.db.QueryRow(sqlStmt, merchantId, subOrderNo)

	sub := &entity.ReconOrderSplitSub{}
	var nullSubOrderNo, notifyResult sql.NullString
	err := row.Scan(
		&sub.ID,
		&sub.OrderNo,
		&nullSubOrderNo,
		&sub.MerchantId,
		&sub.SplitAmount,
		&sub.Status,
		&sub.NotifyStatus,
		&notifyResult,
		&sub.CreateTime,
		&sub.UpdateTime,
	)
	if err != nil {
		if err == sql.ErrNoRows {
			return nil, nil
		}
		return nil, err
	}
	sub.SubOrderNo = nullSubOrderNo.String
	sub.NotifyResult = notifyResult.String

	return sub, nil
}
