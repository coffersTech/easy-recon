from dataclasses import dataclass
from datetime import datetime
from typing import Optional

@dataclass
class ReconNotifyLog:
    """通知日志"""
    order_no: str
    sub_order_no: Optional[str] = None
    merchant_id: Optional[str] = None
    notify_url: Optional[str] = None
    notify_status: int = 0
    notify_result: Optional[str] = None
    
    create_time: Optional[datetime] = None
    update_time: Optional[datetime] = None
