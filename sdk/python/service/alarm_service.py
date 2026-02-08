from abc import ABC, abstractmethod


class AlarmService:
    """告警服务"""

    def __init__(self, strategy):
        self.strategy = strategy

    def send_alarm(self, message: str):
        """发送告警"""
        self.strategy.send_alarm(message)


class AlarmStrategy(ABC):
    """告警策略抽象基类"""

    @abstractmethod
    def send_alarm(self, message: str):
        """发送告警"""
        pass


class LogAlarmStrategy(AlarmStrategy):
    """日志告警策略"""

    def send_alarm(self, message: str):
        """发送告警"""
        # 实现日志告警逻辑
        print(f"[Log] 告警: {message}")


class DingTalkAlarmStrategy(AlarmStrategy):
    """钉钉告警策略"""

    def __init__(self, webhook_url: str):
        self.webhook_url = webhook_url

    def send_alarm(self, message: str):
        """发送告警"""
        # 实现钉钉告警逻辑
        print(f"[DingTalk] 发送告警: {message}")
        # 实际实现中需要调用钉钉机器人 API
