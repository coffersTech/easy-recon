import logging
from decimal import Decimal
from typing import List, Optional
from datetime import datetime
from concurrent.futures import ThreadPoolExecutor

from config.recon_config import ReconConfig
from repository.recon_repository import ReconRepository
from service.alarm_service import AlarmService
from service.exception_record_service import ExceptionRecordService
from entity.recon_order_main import ReconOrderMain
from entity.recon_order_split_sub import ReconOrderSplitSub
from entity.recon_notify_log import ReconNotifyLog
from entity.recon_order_refund_split_sub import ReconOrderRefundSplitSub

logger = logging.getLogger(__name__)

class RealtimeReconService:
    """
    Real-time Reconciliation Service
    """
    def __init__(self, 
                 config: ReconConfig,
                 recon_repository: ReconRepository,
                 exception_record_service: ExceptionRecordService,
                 alarm_service: AlarmService):
        self.config = config
        self.recon_repository = recon_repository
        self.exception_record_service = exception_record_service
        self.alarm_service = alarm_service
        self.executor = ThreadPoolExecutor(max_workers=5) # Simple thread pool

    def recon_order(self, order_no: str, pay_amount: float, platform_income: float,
                   pay_fee: float, split_details: List[ReconOrderSplitSub],
                   pay_status: int, split_status: int, notify_status: int) -> bool:
        try:
            # Defaults
            pay_status = pay_status if pay_status is not None else 0 # PROCESSING
            split_status = split_status if split_status is not None else 0
            notify_status = notify_status if notify_status is not None else 0

            # 1. Check failures
            if pay_status == 2: # FAILURE
                self._record_exception(order_no, "SELF", "Payment failed", 1)
                return False
            if split_status == 2:
                self._record_exception(order_no, "SELF", "Split failed", 2)
                return False
            if notify_status == 2:
                self._record_exception(order_no, "SELF", "Notify failed", 3)
                return False

            # Determine overall status
            recon_status = 1 # SUCCESS
            if pay_status == 0 or split_status == 0 or notify_status == 0:
                recon_status = 0 # PENDING

            # 3. Calculate amounts (only if success)
            split_total = sum(sub.split_amount for sub in split_details if sub.split_amount) if split_details else 0.0
            
            if recon_status == 1:
                calc_amount = split_total + platform_income + pay_fee
                if abs(pay_amount - calc_amount) > self.config.amount_tolerance:
                    self._record_exception(order_no, "SELF", "Amount mismatch", 4)
                    return False

            # 4. Save Order Main
            order_main = ReconOrderMain(
                order_no=order_no,
                pay_amount=pay_amount,
                platform_income=platform_income,
                pay_fee=pay_fee,
                split_total_amount=split_total,
                pay_status=pay_status,
                split_status=split_status,
                notify_status=notify_status,
                recon_status=recon_status,
                create_time=datetime.now(),
                update_time=datetime.now()
            )
            self.recon_repository.save_order_main(order_main)

            # 5. Save Split Subs
            if split_details:
                for sub in split_details:
                    sub.order_no = order_no
                    if sub.notify_status is None:
                        sub.notify_status = notify_status
                self.recon_repository.batch_save_order_split_sub(split_details)

            return True

        except Exception as e:
            logger.error(f"Recon exception: {e}")
            self._record_exception(order_no, "SELF", f"Recon exception: {str(e)}", 5)
            return False

    def recon_notify(self, order_no: Optional[str], merchant_id: str, sub_order_no: Optional[str],
                    merchant_order_no: Optional[str], notify_url: str, notify_status: int,
                    notify_result: str) -> bool:
        try:
            # Locate order_no if missing
            if not order_no and merchant_id:
                if sub_order_no:
                    order_no = self.recon_repository.find_order_no_by_sub(merchant_id, sub_order_no)
                elif merchant_order_no:
                    order_no = self.recon_repository.find_order_no_by_merchant_order(merchant_id, merchant_order_no)
            
            if not order_no:
                return False

            # Update split sub status
            if merchant_id and merchant_id != "SELF":
                self.recon_repository.update_split_sub_notify_status(
                    order_no, merchant_id, sub_order_no, notify_status, notify_result
                )

            # Check if all notified
            all_notified = self.recon_repository.is_all_split_sub_notified(order_no)
            if all_notified:
                self.recon_repository.update_notify_status(order_no, 1, "All merchants notified")
            elif notify_status == 2: # Failure
                 self.recon_repository.update_notify_status(order_no, 2, f"Merchant {merchant_id} notify failed")

            # Log
            log = ReconNotifyLog(
                order_no=order_no,
                sub_order_no=sub_order_no,
                merchant_id=merchant_id,
                notify_url=notify_url,
                notify_status=notify_status,
                notify_result=notify_result
            )
            self.recon_repository.save_notify_log(log)

            # Trigger retry/re-check
            return self.retry_recon(order_no)

        except Exception as e:
            logger.error(f"Notify exception: {e}")
            return False

    def recon_refund(self, order_no: str, refund_amount: float, refund_time: datetime,
                    refund_status: int, split_details: List[ReconOrderRefundSplitSub]) -> bool:
        try:
            refund_status = refund_status if refund_status is not None else 0 # PROCESSING
            
            # 1. Check original order
            order_main = self.recon_repository.get_order_main(order_no)
            if not order_main:
                return False

            # 2. Check amount
            if refund_amount > order_main.pay_amount:
                self._record_exception(order_no, "SELF", "Refund amount > Pay amount", 4)
                return False

            # 3. Check split total
            split_total = sum(sub.refund_split_amount for sub in split_details if sub.refund_split_amount) if split_details else 0.0
            if split_total > refund_amount:
                 self._record_exception(order_no, "SELF", "Refund split total > Refund amount", 4)
                 return False

            # 4. Update order refund status
            success = self.recon_repository.update_recon_refund_status(order_no, refund_status, refund_amount, refund_time)
            if not success:
                return False

            # 5. Save split details
            if split_details:
                for sub in split_details:
                    sub.order_no = order_no
                self.recon_repository.batch_save_order_refund_split_sub(split_details)
            
            return True

        except Exception as e:
            logger.error(f"Refund exception: {e}")
            self._record_exception(order_no, "SELF", f"Refund exception: {str(e)}", 5)
            return False

    def retry_recon(self, order_no: str) -> bool:
        try:
            order_main = self.recon_repository.get_order_main(order_no)
            if not order_main:
                return False
            
            if order_main.recon_status == 1: # SUCCESS
                return True
            
            # Check if still processing
            if (order_main.pay_status == 0 or 
                order_main.split_status == 0 or 
                order_main.notify_status == 0):
                return False
            
            # Re-verify amount
            split_subs = self.recon_repository.get_split_subs(order_no)
            split_total = sum(sub.split_amount for sub in split_subs if sub.split_amount)
            calc_amount = split_total + order_main.platform_income + order_main.pay_fee
            
            if abs(order_main.pay_amount - calc_amount) > self.config.amount_tolerance:
                 self._record_exception(order_no, "SELF", "Retry: Amount mismatch", 4)
                 self.recon_repository.update_recon_status(order_no, 2) # FAILURE
                 return False
            
            return self.recon_repository.update_recon_status(order_no, 1) # SUCCESS

        except Exception as e:
            logger.error(f"Retry exception: {e}")
            return False

    def recon_refund_by_sub(self, merchant_id: str, sub_order_no: str, refund_amount: float,
                           refund_time: datetime, refund_status: int) -> bool:
        try:
            order_no = self.recon_repository.find_order_no_by_sub(merchant_id, sub_order_no)
            if not order_no:
                return False
            
            # Simple refund (assume one split for sub)
            refund_splits = [
                ReconOrderRefundSplitSub(
                    sub_order_no=sub_order_no,
                    merchant_id=merchant_id,
                    refund_split_amount=refund_amount
                )
            ]
            return self.recon_refund(order_no, refund_amount, refund_time, refund_status, refund_splits)
        except Exception as e:
            logger.error(f"Refund by sub exception: {e}")
            return False

    def recon_notify_by_merchant_order(self, merchant_id: str, merchant_order_no: str, notify_url: str,
                                      notify_status: int, notify_result: str) -> bool:
        try:
            order_no = self.recon_repository.find_order_no_by_merchant_order(merchant_id, merchant_order_no)
            if not order_no:
                return False
            # Find sub_order_no if needed, but for now we just need order_no to update status
            # However, logic in recon_notify updates split_sub status by merchant_id and sub_order_no
            # We need to find the sub_order_no associated with this merchant_order_no to be precise
            # For simplicity, we assume we can update split sub by merchant_order_no or find it first
            
            # Let's find sub_order_no first to be safe, or update recon_notify to handle it
            # recon_notify(self, order_no, merchant_id, sub_order_no, merchant_order_no...)
            return self.recon_notify(order_no, merchant_id, None, merchant_order_no, notify_url, notify_status, notify_result)
        except Exception as e:
            logger.error(f"Notify by merchant order exception: {e}")
            return False

    def recon_refund_by_merchant_order(self, merchant_id: str, merchant_order_no: str, refund_amount: float,
                                      refund_time: datetime, refund_status: int) -> bool:
        try:
            order_no = self.recon_repository.find_order_no_by_merchant_order(merchant_id, merchant_order_no)
            if not order_no:
                return False
            
            refund_splits = [
                ReconOrderRefundSplitSub(
                    merchant_id=merchant_id,
                    merchant_order_no=merchant_order_no,
                    refund_split_amount=refund_amount
                )
            ]
            return self.recon_refund(order_no, refund_amount, refund_time, refund_status, refund_splits)
        except Exception as e:
            logger.error(f"Refund by merchant order exception: {e}")
            return False

    def _record_exception(self, order_no: str, merchant_id: str, msg: str, step: int):
        self.exception_record_service.record_recon_exception(order_no, merchant_id, msg, step)
        self.alarm_service.send_recon_alarm(order_no, merchant_id, msg)
