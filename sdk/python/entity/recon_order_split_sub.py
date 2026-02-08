from dataclasses import dataclass
from datetime import datetime


@dataclass
class ReconOrderSplitSub:
    """对账订单分账子记录"""
    id: int = None
    order_no: str = None
    sub_order_no: str = None
    merchant_id: str = None
    split_amount: float = None
    status: int = None
    create_time: datetime = None
    update_time: datetime = None
