from dataclasses import dataclass
from datetime import datetime
from typing import Optional

@dataclass
class ReconOrderMain:
    """对账订单主记录"""
    order_no: str
    pay_amount: float
    platform_income: float
    pay_fee: float
    split_total_amount: float
    
    # 状态枚举值 (0: PENDING, 1: SUCCESS, 2: FAILURE)
    pay_status: int
    split_status: int
    notify_status: int
    recon_status: int
    
    notify_result: Optional[str] = None
    
    create_time: Optional[datetime] = None
    update_time: Optional[datetime] = None
    
    # 退款相关
    refund_status: Optional[int] = None
    refund_amount: Optional[float] = None
    refund_time: Optional[datetime] = None
