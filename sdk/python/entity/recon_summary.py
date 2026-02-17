from dataclasses import dataclass
from decimal import Decimal

@dataclass
class ReconSummary:
    total_orders: int = 0
    success_count: int = 0
    fail_count: int = 0
    total_amount: Decimal = Decimal("0.00")
