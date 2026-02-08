from typing import List
from entity.recon_order_main import ReconOrderMain
from entity.recon_order_split_sub import ReconOrderSplitSub
from repository.recon_repository import ReconRepository
from service.alarm_service import AlarmService


class RealtimeReconService:
    """实时对账服务"""

    def __init__(self, recon_repository: ReconRepository, alarm_service: AlarmService):
        self.recon_repository = recon_repository
        self.alarm_service = alarm_service

    def do_realtime_recon(self, order_main: ReconOrderMain, split_subs: List[ReconOrderSplitSub]) -> bool:
        """执行实时对账"""
        # 1. 保存订单主记录
        main_saved = self.recon_repository.save_order_main(order_main)
        if not main_saved:
            self.alarm_service.send_alarm("保存订单主记录失败")
            return False

        # 2. 批量保存分账子记录
        if split_subs:
            sub_saved = self.recon_repository.batch_save_order_split_sub(split_subs)
            if not sub_saved:
                self.alarm_service.send_alarm("批量保存分账子记录失败")
                return False

        # 3. 更新对账状态为已对账
        status_updated = self.recon_repository.update_recon_status(order_main.order_no, 1)  # 1: 已对账
        if not status_updated:
            self.alarm_service.send_alarm("更新对账状态失败")
            return False

        return True
