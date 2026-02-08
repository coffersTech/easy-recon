from typing import List
from entity.recon_order_main import ReconOrderMain
from entity.recon_order_split_sub import ReconOrderSplitSub
from service.realtime_recon_service import RealtimeReconService
from service.timing_recon_service import TimingReconService


class EasyReconTemplate:
    """对账SDK核心模板类"""

    def __init__(self, realtime_recon_service: RealtimeReconService, timing_recon_service: TimingReconService):
        self.realtime_recon_service = realtime_recon_service
        self.timing_recon_service = timing_recon_service

    def do_realtime_recon(self, order_main: ReconOrderMain, split_subs: List[ReconOrderSplitSub]) -> bool:
        """执行实时对账"""
        return self.realtime_recon_service.do_realtime_recon(order_main, split_subs)

    def do_timing_recon(self, date_str: str) -> bool:
        """执行定时对账"""
        return self.timing_recon_service.do_timing_recon(date_str)
