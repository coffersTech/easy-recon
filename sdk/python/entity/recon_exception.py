from dataclasses import dataclass
from datetime import datetime
from typing import Optional

@dataclass
class ReconException:
    """对账异常记录"""
    order_no: str
    merchant_id: Optional[str] = None
    exception_msg: Optional[str] = None
    exception_step: Optional[int] = None
    
    create_time: Optional[datetime] = None
    update_time: Optional[datetime] = None
