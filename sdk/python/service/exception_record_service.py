import logging
from typing import Optional
from repository.recon_repository import ReconRepository
from entity.recon_exception import ReconException

logger = logging.getLogger(__name__)

class ExceptionRecordService:
    """
    Service for recording reconciliation exceptions
    """
    def __init__(self, recon_repository: ReconRepository):
        self.recon_repository = recon_repository

    def record_recon_exception(self, order_no: str, merchant_id: Optional[str], msg: str, step: int):
        try:
            exception = ReconException(
                order_no=order_no,
                merchant_id=merchant_id,
                exception_msg=msg,
                exception_step=step
            )
            self.recon_repository.save_exception(exception)
        except Exception as e:
            logger.error(f"Failed to record exception: {e}")
