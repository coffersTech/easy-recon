from dataclasses import dataclass
from datetime import datetime


@dataclass
class ReconOrderMain:
    """对账订单主记录"""
    id: int = None
    order_no: str = None
    merchant_id: str = None
    merchant_name: str = None
    order_amount: float = None
    actual_amount: float = None
    recon_status: int = None
    order_time: datetime = None
    pay_time: datetime = None
    recon_time: datetime = None
    create_time: datetime = None
    update_time: datetime = None
