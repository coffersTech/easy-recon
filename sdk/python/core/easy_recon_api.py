from typing import List, Optional
from concurrent.futures import Future, ThreadPoolExecutor
from decimal import Decimal
from datetime import datetime

from service.realtime_recon_service import RealtimeReconService
from service.timing_recon_service import TimingReconService
from repository.recon_repository import ReconRepository
from entity.recon_order_split_sub import ReconOrderSplitSub
from entity.recon_order_refund_split_sub import ReconOrderRefundSplitSub
from entity.recon_order_main import ReconOrderMain
from entity.recon_exception import ReconException
from entity.recon_notify_log import ReconNotifyLog
from entity.recon_summary import ReconSummary

class EasyReconApi:
    """
    Easy Recon SDK Public API
    """
    def __init__(self, 
                 realtime_recon_service: RealtimeReconService,
                 timing_recon_service: TimingReconService,
                 recon_repository: ReconRepository):
        self.realtime_recon_service = realtime_recon_service
        self.timing_recon_service = timing_recon_service
        self.recon_repository = recon_repository
        self.executor = ThreadPoolExecutor(max_workers=5)

    def recon_order(self, order_no: str, pay_amount: float, platform_income: float,
                   pay_fee: float, split_details: List[ReconOrderSplitSub],
                   pay_status: int, split_status: int, notify_status: int) -> bool:
        """
        Reconcile an order synchronously
        """
        return self.realtime_recon_service.recon_order(
            order_no, pay_amount, platform_income, pay_fee, split_details,
            pay_status, split_status, notify_status
        )

    def recon_order_async(self, order_no: str, pay_amount: float, platform_income: float,
                         pay_fee: float, split_details: List[ReconOrderSplitSub],
                         pay_status: int, split_status: int, notify_status: int) -> Future:
        """
        Reconcile an order asynchronously
        """
        return self.executor.submit(
            self.recon_order,
            order_no, pay_amount, platform_income, pay_fee, split_details,
            pay_status, split_status, notify_status
        )

    def recon_notify(self, order_no: str, merchant_id: str, notify_url: str,
                    notify_status: int, notify_result: str) -> bool:
        """
        Handle reconciliation notification
        """
        return self.realtime_recon_service.recon_notify(
            order_no, merchant_id, None, None, notify_url, notify_status, notify_result
        )

    def recon_notify_by_sub(self, merchant_id: str, sub_order_no: str, notify_url: str,
                           notify_status: int, notify_result: str) -> bool:
        """
        Handle reconciliation notification by sub-order
        """
        return self.realtime_recon_service.recon_notify(
            None, merchant_id, sub_order_no, None, notify_url, notify_status, notify_result
        )

    def recon_refund(self, order_no: str, refund_amount: float, refund_time: str, # refund_time as str or datetime?
                    refund_status: int, split_details: List[ReconOrderRefundSplitSub]) -> bool:
        """
        Reconcile a refund
        """
        return self.realtime_recon_service.recon_refund(
            order_no, refund_amount, refund_time, refund_status, split_details
        )

    def recon_refund_by_sub(self, merchant_id: str, sub_order_no: str, refund_amount: float,
                           refund_time: datetime, refund_status: int) -> bool:
        return self.realtime_recon_service.recon_refund_by_sub(
            merchant_id, sub_order_no, refund_amount, refund_time, refund_status
        )

    def recon_notify_by_merchant_order(self, merchant_id: str, merchant_order_no: str, notify_url: str,
                                      notify_status: int, notify_result: str) -> bool:
        return self.realtime_recon_service.recon_notify_by_merchant_order(
            merchant_id, merchant_order_no, notify_url, notify_status, notify_result
        )

    def recon_refund_by_merchant_order(self, merchant_id: str, merchant_order_no: str, refund_amount: float,
                                      refund_time: datetime, refund_status: int) -> bool:
        return self.realtime_recon_service.recon_refund_by_merchant_order(
            merchant_id, merchant_order_no, refund_amount, refund_time, refund_status
        )
    
    # Query Methods
    def get_order_main(self, order_no: str) -> Optional[ReconOrderMain]:
        return self.recon_repository.get_order_main(order_no)

    def get_recon_status(self, order_no: str) -> Optional[int]:
        order = self.recon_repository.get_order_main(order_no)
        return order.recon_status if order else None

    def get_recon_summary(self, date_str: str) -> ReconSummary:
        return self.recon_repository.get_recon_summary(date_str)

    def list_orders_by_date(self, date_str: str, page: int = 1, size: int = 10) -> tuple[List[ReconOrderMain], int]:
        return self.recon_repository.list_orders_by_date(date_str, page, size)
