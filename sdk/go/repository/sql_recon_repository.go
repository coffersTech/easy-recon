package repository

import (
	"database/sql"
	"github.com/coffersTech/easy-recon/sdk/go/dialect"
	"github.com/coffersTech/easy-recon/sdk/go/entity"
	"fmt"
)

// SQLReconRepository SQL实现的对账存储库
type SQLReconRepository struct {
	db      *sql.DB
	dialect dialect.ReconDatabaseDialect
}

// NewSQLReconRepository 创建SQL对账存储库
func NewSQLReconRepository(db *sql.DB, dialect dialect.ReconDatabaseDialect) ReconRepository {
	return &SQLReconRepository{
		db:      db,
		dialect: dialect,
	}
}

// SaveOrderMain 保存对账订单主记录
func (r *SQLReconRepository) SaveOrderMain(orderMain *entity.ReconOrderMain) (bool, error) {
	sqlStmt := "INSERT INTO recon_order_main " +
		"(order_no, merchant_id, merchant_name, order_amount, actual_amount, recon_status, " +
		"order_time, pay_time, recon_time, create_time, update_time) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"

	_, err := r.db.Exec(sqlStmt,
		orderMain.OrderNo,
		orderMain.MerchantId,
		orderMain.MerchantName,
		orderMain.OrderAmount,
		orderMain.ActualAmount,
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

	sqlStmt := "INSERT INTO recon_order_split_sub " +
		"(order_no, sub_order_no, merchant_id, split_amount, status, create_time, update_time) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?)"

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
	sqlStmt := "INSERT INTO recon_exception " +
		"(order_no, merchant_id, exception_type, exception_msg, exception_step, " +
		"create_time, update_time) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?)"

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

	sqlStmt := "INSERT INTO recon_exception " +
		"(order_no, merchant_id, exception_type, exception_msg, exception_step, " +
		"create_time, update_time) " +
		"VALUES (?, ?, ?, ?, ?, ?, ?)"

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
	sqlStmt := "SELECT * FROM recon_order_main WHERE order_no = ?"
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
	sqlStmt := "SELECT * FROM recon_order_split_sub WHERE order_no = ?"
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
	sql := fmt.Sprintf("SELECT * FROM recon_order_main "+
		"WHERE %s(order_time) = ? AND recon_status = 0 "+
		"LIMIT ? OFFSET ?", r.dialect.GetDateFunction())

	rows, err := r.db.Query(sql, dateStr, limit, offset)
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
	sql := fmt.Sprintf("UPDATE recon_order_main SET recon_status = ?, update_time = %s WHERE order_no = ?", r.dialect.GetCurrentTimeFunction())

	_, err := r.db.Exec(sql, reconStatus, orderNo)
	if err != nil {
		return false, err
	}

	return true, nil
}

// GetOrderMainByMerchantId 根据商户ID查询对账订单主记录（分页）
func (r *SQLReconRepository) GetOrderMainByMerchantId(merchantId, startDate, endDate string, reconStatus *int, offset, limit int) ([]*entity.ReconOrderMain, error) {
	// 构建查询SQL
	query := "SELECT * FROM recon_order_main WHERE merchant_id = ?"
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

	rows, err := r.db.Query(query, args...)
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
	query := fmt.Sprintf("SELECT * FROM recon_order_main WHERE %s(order_time) = ?", r.dialect.GetDateFunction())
	args := []interface{}{dateStr}

	if reconStatus != nil {
		query += " AND recon_status = ?"
		args = append(args, *reconStatus)
	}

	query += " LIMIT ? OFFSET ?"
	args = append(args, limit, offset)

	rows, err := r.db.Query(query, args...)
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
	query := "SELECT * FROM recon_exception WHERE 1=1"
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

	rows, err := r.db.Query(query, args...)
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
	sqlStmt := "SELECT * FROM recon_exception WHERE order_no = ?"
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
	err := row.Scan(
		&orderMain.ID,
		&orderMain.OrderNo,
		&orderMain.MerchantId,
		&orderMain.MerchantName,
		&orderMain.OrderAmount,
		&orderMain.ActualAmount,
		&orderMain.ReconStatus,
		&orderMain.OrderTime,
		&orderMain.PayTime,
		&orderMain.ReconTime,
		&orderMain.CreateTime,
		&orderMain.UpdateTime,
	)
	if err != nil {
		return nil, err
	}
	return orderMain, nil
}

// 辅助方法：从结果集扫描订单主记录
func (r *SQLReconRepository) scanOrderMainFromRows(rows *sql.Rows) (*entity.ReconOrderMain, error) {
	orderMain := &entity.ReconOrderMain{}
	err := rows.Scan(
		&orderMain.ID,
		&orderMain.OrderNo,
		&orderMain.MerchantId,
		&orderMain.MerchantName,
		&orderMain.OrderAmount,
		&orderMain.ActualAmount,
		&orderMain.ReconStatus,
		&orderMain.OrderTime,
		&orderMain.PayTime,
		&orderMain.ReconTime,
		&orderMain.CreateTime,
		&orderMain.UpdateTime,
	)
	if err != nil {
		return nil, err
	}
	return orderMain, nil
}

// 辅助方法：从结果集扫描分账子记录
func (r *SQLReconRepository) scanOrderSplitSub(rows *sql.Rows) (*entity.ReconOrderSplitSub, error) {
	sub := &entity.ReconOrderSplitSub{}
	err := rows.Scan(
		&sub.ID,
		&sub.OrderNo,
		&sub.SubOrderNo,
		&sub.MerchantId,
		&sub.SplitAmount,
		&sub.Status,
		&sub.CreateTime,
		&sub.UpdateTime,
	)
	if err != nil {
		return nil, err
	}
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
