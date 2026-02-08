from typing import List, Optional
import mysql.connector
import psycopg2
from datetime import datetime
from entity.recon_order_main import ReconOrderMain
from entity.recon_order_split_sub import ReconOrderSplitSub
from entity.recon_exception import ReconException
from repository.recon_repository import ReconRepository
from dialect.recon_database_dialect import ReconDatabaseDialect


class SQLReconRepository(ReconRepository):
    """SQL实现的对账存储库"""

    def __init__(self, connection, dialect: ReconDatabaseDialect):
        self.connection = connection
        self.dialect = dialect

    def save_order_main(self, order_main: ReconOrderMain) -> bool:
        """保存对账订单主记录"""
        sql = "INSERT INTO recon_order_main " \
              "(order_no, merchant_id, merchant_name, order_amount, actual_amount, recon_status, " \
              "order_time, pay_time, recon_time, create_time, update_time) " \
              "VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"

        cursor = self.connection.cursor()
        try:
            cursor.execute(sql, (
                order_main.order_no,
                order_main.merchant_id,
                order_main.merchant_name,
                order_main.order_amount,
                order_main.actual_amount,
                order_main.recon_status,
                order_main.order_time,
                order_main.pay_time,
                order_main.recon_time,
                order_main.create_time,
                order_main.update_time
            ))
            self.connection.commit()
            return True
        except Exception as e:
            self.connection.rollback()
            print(f"保存订单主记录失败: {e}")
            return False
        finally:
            cursor.close()

    def batch_save_order_split_sub(self, split_subs: List[ReconOrderSplitSub]) -> bool:
        """批量保存分账子记录"""
        if not split_subs:
            return True

        sql = "INSERT INTO recon_order_split_sub " \
              "(order_no, sub_order_no, merchant_id, split_amount, status, create_time, update_time) " \
              "VALUES (%s, %s, %s, %s, %s, %s, %s)"

        cursor = self.connection.cursor()
        try:
            for sub in split_subs:
                cursor.execute(sql, (
                    sub.order_no,
                    sub.sub_order_no,
                    sub.merchant_id,
                    sub.split_amount,
                    sub.status,
                    sub.create_time,
                    sub.update_time
                ))
            self.connection.commit()
            return True
        except Exception as e:
            self.connection.rollback()
            print(f"批量保存分账子记录失败: {e}")
            return False
        finally:
            cursor.close()

    def save_exception(self, exception: ReconException) -> bool:
        """保存异常记录"""
        sql = "INSERT INTO recon_exception " \
              "(order_no, merchant_id, exception_type, exception_msg, exception_step, " \
              "create_time, update_time) " \
              "VALUES (%s, %s, %s, %s, %s, %s, %s)"

        cursor = self.connection.cursor()
        try:
            cursor.execute(sql, (
                exception.order_no,
                exception.merchant_id,
                exception.exception_type,
                exception.exception_msg,
                exception.exception_step,
                exception.create_time,
                exception.update_time
            ))
            self.connection.commit()
            return True
        except Exception as e:
            self.connection.rollback()
            print(f"保存异常记录失败: {e}")
            return False
        finally:
            cursor.close()

    def batch_save_exception(self, exceptions: List[ReconException]) -> bool:
        """批量保存异常记录"""
        if not exceptions:
            return True

        sql = "INSERT INTO recon_exception " \
              "(order_no, merchant_id, exception_type, exception_msg, exception_step, " \
              "create_time, update_time) " \
              "VALUES (%s, %s, %s, %s, %s, %s, %s)"

        cursor = self.connection.cursor()
        try:
            for exception in exceptions:
                cursor.execute(sql, (
                    exception.order_no,
                    exception.merchant_id,
                    exception.exception_type,
                    exception.exception_msg,
                    exception.exception_step,
                    exception.create_time,
                    exception.update_time
                ))
            self.connection.commit()
            return True
        except Exception as e:
            self.connection.rollback()
            print(f"批量保存异常记录失败: {e}")
            return False
        finally:
            cursor.close()

    def get_order_main_by_order_no(self, order_no: str) -> Optional[ReconOrderMain]:
        """根据订单号查询对账订单主记录"""
        sql = "SELECT * FROM recon_order_main WHERE order_no = %s"

        cursor = self.connection.cursor()
        try:
            cursor.execute(sql, (order_no,))
            row = cursor.fetchone()
            if row:
                return self._map_to_order_main(row)
            return None
        except Exception as e:
            print(f"查询订单主记录失败: {e}")
            return None
        finally:
            cursor.close()

    def get_order_split_sub_by_order_no(self, order_no: str) -> List[ReconOrderSplitSub]:
        """根据订单号查询分账子记录"""
        sql = "SELECT * FROM recon_order_split_sub WHERE order_no = %s"

        cursor = self.connection.cursor()
        try:
            cursor.execute(sql, (order_no,))
            rows = cursor.fetchall()
            result = []
            for row in rows:
                result.append(self._map_to_order_split_sub(row))
            return result
        except Exception as e:
            print(f"查询分账子记录失败: {e}")
            return []
        finally:
            cursor.close()

    def get_pending_recon_orders(self, date_str: str, offset: int, limit: int) -> List[ReconOrderMain]:
        """查询指定日期的待核账订单（分页）"""
        sql = f"SELECT * FROM recon_order_main " \
              f"WHERE {self.dialect.get_date_function()}(order_time) = %s AND recon_status = 0 " \
              f"LIMIT %s OFFSET %s"

        cursor = self.connection.cursor()
        try:
            cursor.execute(sql, (date_str, limit, offset))
            rows = cursor.fetchall()
            result = []
            for row in rows:
                result.append(self._map_to_order_main(row))
            return result
        except Exception as e:
            print(f"查询待核账订单失败: {e}")
            return []
        finally:
            cursor.close()

    def update_recon_status(self, order_no: str, recon_status: int) -> bool:
        """更新对账状态"""
        sql = f"UPDATE recon_order_main SET recon_status = %s, update_time = {self.dialect.get_current_time_function()} WHERE order_no = %s"

        cursor = self.connection.cursor()
        try:
            cursor.execute(sql, (recon_status, order_no))
            self.connection.commit()
            return cursor.rowcount > 0
        except Exception as e:
            self.connection.rollback()
            print(f"更新对账状态失败: {e}")
            return False
        finally:
            cursor.close()

    def get_order_main_by_merchant_id(self, merchant_id: str, start_date: str, end_date: str, 
                                     recon_status: Optional[int], offset: int, limit: int) -> List[ReconOrderMain]:
        """根据商户ID查询对账订单主记录（分页）"""
        conditions = ["merchant_id = %s"]
        params = [merchant_id]

        if start_date:
            conditions.append(f"{self.dialect.get_date_function()}(order_time) >= %s")
            params.append(start_date)

        if end_date:
            conditions.append(f"{self.dialect.get_date_function()}(order_time) <= %s")
            params.append(end_date)

        if recon_status is not None:
            conditions.append("recon_status = %s")
            params.append(recon_status)

        sql = f"SELECT * FROM recon_order_main WHERE {' AND '.join(conditions)} " \
              f"LIMIT %s OFFSET %s"
        params.extend([limit, offset])

        cursor = self.connection.cursor()
        try:
            cursor.execute(sql, params)
            rows = cursor.fetchall()
            result = []
            for row in rows:
                result.append(self._map_to_order_main(row))
            return result
        except Exception as e:
            print(f"根据商户ID查询订单主记录失败: {e}")
            return []
        finally:
            cursor.close()

    def get_order_main_by_date(self, date_str: str, recon_status: Optional[int], 
                              offset: int, limit: int) -> List[ReconOrderMain]:
        """根据日期查询对账订单主记录（分页）"""
        conditions = [f"{self.dialect.get_date_function()}(order_time) = %s"]
        params = [date_str]

        if recon_status is not None:
            conditions.append("recon_status = %s")
            params.append(recon_status)

        sql = f"SELECT * FROM recon_order_main WHERE {' AND '.join(conditions)} " \
              f"LIMIT %s OFFSET %s"
        params.extend([limit, offset])

        cursor = self.connection.cursor()
        try:
            cursor.execute(sql, params)
            rows = cursor.fetchall()
            result = []
            for row in rows:
                result.append(self._map_to_order_main(row))
            return result
        except Exception as e:
            print(f"根据日期查询订单主记录失败: {e}")
            return []
        finally:
            cursor.close()

    def get_exception_records(self, merchant_id: str, start_date: str, end_date: str, 
                             exception_step: Optional[int], offset: int, limit: int) -> List[ReconException]:
        """查询对账异常记录（分页）"""
        conditions = []
        params = []

        if merchant_id:
            conditions.append("merchant_id = %s")
            params.append(merchant_id)

        if start_date:
            conditions.append(f"{self.dialect.get_date_function()}(create_time) >= %s")
            params.append(start_date)

        if end_date:
            conditions.append(f"{self.dialect.get_date_function()}(create_time) <= %s")
            params.append(end_date)

        if exception_step is not None:
            conditions.append("exception_step = %s")
            params.append(exception_step)

        where_clause = "WHERE " + " AND ".join(conditions) if conditions else ""
        sql = f"SELECT * FROM recon_exception {where_clause} " \
              f"LIMIT %s OFFSET %s"
        params.extend([limit, offset])

        cursor = self.connection.cursor()
        try:
            cursor.execute(sql, params)
            rows = cursor.fetchall()
            result = []
            for row in rows:
                result.append(self._map_to_exception(row))
            return result
        except Exception as e:
            print(f"查询异常记录失败: {e}")
            return []
        finally:
            cursor.close()

    def get_exception_by_order_no(self, order_no: str) -> Optional[ReconException]:
        """根据订单号查询对账异常记录"""
        sql = "SELECT * FROM recon_exception WHERE order_no = %s"

        cursor = self.connection.cursor()
        try:
            cursor.execute(sql, (order_no,))
            row = cursor.fetchone()
            if row:
                return self._map_to_exception(row)
            return None
        except Exception as e:
            print(f"查询异常记录失败: {e}")
            return None
        finally:
            cursor.close()

    def _map_to_order_main(self, row) -> ReconOrderMain:
        """将数据库行映射为订单主记录"""
        return ReconOrderMain(
            id=row[0],
            order_no=row[1],
            merchant_id=row[2],
            merchant_name=row[3],
            order_amount=row[4],
            actual_amount=row[5],
            recon_status=row[6],
            order_time=row[7],
            pay_time=row[8],
            recon_time=row[9],
            create_time=row[10],
            update_time=row[11]
        )

    def _map_to_order_split_sub(self, row) -> ReconOrderSplitSub:
        """将数据库行映射为分账子记录"""
        return ReconOrderSplitSub(
            id=row[0],
            order_no=row[1],
            sub_order_no=row[2],
            merchant_id=row[3],
            split_amount=row[4],
            status=row[5],
            create_time=row[6],
            update_time=row[7]
        )

    def _map_to_exception(self, row) -> ReconException:
        """将数据库行映射为异常记录"""
        return ReconException(
            id=row[0],
            order_no=row[1],
            merchant_id=row[2],
            exception_type=row[3],
            exception_msg=row[4],
            exception_step=row[5],
            create_time=row[6],
            update_time=row[7]
        )
