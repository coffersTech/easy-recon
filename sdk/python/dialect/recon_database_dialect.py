from abc import ABC, abstractmethod
import mysql.connector
import psycopg2


class ReconDatabaseDialect(ABC):
    """数据库方言抽象基类"""

    @abstractmethod
    def get_database_type(self) -> str:
        """获取数据库类型名称"""
        pass

    @abstractmethod
    def get_date_function(self) -> str:
        """获取日期函数"""
        pass

    @abstractmethod
    def get_current_time_function(self) -> str:
        """获取当前时间函数"""
        pass


class MySQLDialect(ReconDatabaseDialect):
    """MySQL方言实现"""

    def get_database_type(self) -> str:
        return "mysql"

    def get_date_function(self) -> str:
        return "DATE"

    def get_current_time_function(self) -> str:
        return "NOW()"


class PostgreSQLDialect(ReconDatabaseDialect):
    """PostgreSQL方言实现"""

    def get_database_type(self) -> str:
        return "postgresql"

    def get_date_function(self) -> str:
        return "DATE"

    def get_current_time_function(self) -> str:
        return "CURRENT_TIMESTAMP"


def create_dialect(connection) -> ReconDatabaseDialect:
    """创建数据库方言"""
    # 通过连接对象判断数据库类型
    if isinstance(connection, mysql.connector.connection.MySQLConnection):
        return MySQLDialect()
    elif isinstance(connection, psycopg2.extensions.connection):
        return PostgreSQLDialect()
    else:
        # 默认返回MySQL方言
        return MySQLDialect()
