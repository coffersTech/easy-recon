from abc import ABC, abstractmethod
from typing import List, Optional
from entity.recon_order_main import ReconOrderMain
from entity.recon_order_split_sub import ReconOrderSplitSub
from entity.recon_exception import ReconException


class ReconRepository(ABC):
    """对账存储库抽象基类"""

    @abstractmethod
    def save_order_main(self, order_main: ReconOrderMain) -> bool:
        """保存对账订单主记录"""
        pass

    @abstractmethod
    def batch_save_order_split_sub(self, split_subs: List[ReconOrderSplitSub]) -> bool:
        """批量保存分账子记录"""
        pass

    @abstractmethod
    def save_exception(self, exception: ReconException) -> bool:
        """保存异常记录"""
        pass

    @abstractmethod
    def batch_save_exception(self, exceptions: List[ReconException]) -> bool:
        """批量保存异常记录"""
        pass

    @abstractmethod
    def get_order_main_by_order_no(self, order_no: str) -> Optional[ReconOrderMain]:
        """根据订单号查询对账订单主记录"""
        pass

    @abstractmethod
    def get_order_split_sub_by_order_no(self, order_no: str) -> List[ReconOrderSplitSub]:
        """根据订单号查询分账子记录"""
        pass

    @abstractmethod
    def get_pending_recon_orders(self, date_str: str, offset: int, limit: int) -> List[ReconOrderMain]:
        """查询指定日期的待核账订单（分页）"""
        pass

    @abstractmethod
    def update_recon_status(self, order_no: str, recon_status: int) -> bool:
        """更新对账状态"""
        pass

    @abstractmethod
    def get_order_main_by_merchant_id(self, merchant_id: str, start_date: str, end_date: str, 
                                      recon_status: Optional[int], offset: int, limit: int) -> List[ReconOrderMain]:
        """根据商户ID查询对账订单主记录（分页）"""
        pass

    @abstractmethod
    def get_order_main_by_date(self, date_str: str, recon_status: Optional[int], 
                               offset: int, limit: int) -> List[ReconOrderMain]:
        """根据日期查询对账订单主记录（分页）"""
        pass

    @abstractmethod
    def get_exception_records(self, merchant_id: str, start_date: str, end_date: str, 
                             exception_step: Optional[int], offset: int, limit: int) -> List[ReconException]:
        """查询对账异常记录（分页）"""
        pass

    @abstractmethod
    def get_exception_by_order_no(self, order_no: str) -> Optional[ReconException]:
        """根据订单号查询对账异常记录"""
        pass
