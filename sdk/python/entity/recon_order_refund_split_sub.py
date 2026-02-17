from dataclasses import dataclass
from datetime import datetime
from typing import Optional

@dataclass
class ReconOrderRefundSplitSub:
    """退款分账子记录"""
    order_no: Optional[str] = None
    sub_order_no: Optional[str] = None
    merchant_id: Optional[str] = None
    merchant_order_no: Optional[str] = None
    refund_split_amount: Optional[float] = None
    
    create_time: Optional[datetime] = None
    update_time: Optional[datetime] = None
