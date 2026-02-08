from dataclasses import dataclass
from datetime import datetime


@dataclass
class ReconException:
    """对账异常记录"""
    id: int = None
    order_no: str = None
    merchant_id: str = None
    exception_type: int = None
    exception_msg: str = None
    exception_step: int = None
    create_time: datetime = None
    update_time: datetime = None
