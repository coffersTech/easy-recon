from repository.recon_repository import ReconRepository
from service.alarm_service import AlarmService


class TimingReconService:
    """定时对账服务"""

    def __init__(self, recon_repository: ReconRepository, alarm_service: AlarmService):
        self.recon_repository = recon_repository
        self.alarm_service = alarm_service

    def do_timing_recon(self, date_str: str) -> bool:
        """执行定时对账"""
        total_processed = 0
        limit = 100
        offset = 0

        while True:
            # 查询待核账订单
            pending_orders = self.recon_repository.get_pending_recon_orders(date_str, offset, limit)
            if not pending_orders:
                break

            # 处理每个待核账订单
            for order in pending_orders:
                if not self._process_pending_order(order):
                    self.alarm_service.send_alarm(f"处理订单 {order.order_no} 失败")
                total_processed += 1

            offset += limit

        self.alarm_service.send_alarm(f"定时对账完成，共处理 {total_processed} 笔订单")
        return True

    def _process_pending_order(self, order) -> bool:
        """处理待核账订单"""
        # 这里可以添加更复杂的对账逻辑
        # 例如：与第三方支付平台对账、金额校验等

        # 更新对账状态为已对账
        return self.recon_repository.update_recon_status(order.order_no, 1)  # 1: 已对账
