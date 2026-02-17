import logging
from typing import Optional

logger = logging.getLogger(__name__)

class AlarmService:
    """
    Alarm Service for sending alerts
    """
    def send_recon_alarm(self, order_no: str, merchant_id: Optional[str], msg: str):
        # In a real implementation, this would send an email or Slack message
        # For now, we just log it as an error/warning
        logger.error(f"[ALARM] Order: {order_no}, Merchant: {merchant_id}, Msg: {msg}")
