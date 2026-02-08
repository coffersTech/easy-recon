package service

// AlarmService 告警服务
type AlarmService struct {
	strategy AlarmStrategy
}

// NewAlarmService 创建告警服务
func NewAlarmService(strategy AlarmStrategy) *AlarmService {
	return &AlarmService{
		strategy: strategy,
	}
}

// SendAlarm 发送告警
func (s *AlarmService) SendAlarm(message string) {
	s.strategy.SendAlarm(message)
}

// AlarmStrategy 告警策略接口
type AlarmStrategy interface {
	// SendAlarm 发送告警
	SendAlarm(message string)
}

// LogAlarmStrategy 日志告警策略
type LogAlarmStrategy struct{}

// SendAlarm 发送告警
func (s *LogAlarmStrategy) SendAlarm(message string) {
	// 实现日志告警逻辑
	println("[Log] 告警: " + message)
}

// DingTalkAlarmStrategy 钉钉告警策略
type DingTalkAlarmStrategy struct {
	webhookUrl string
}

// NewDingTalkAlarmStrategy 创建钉钉告警策略
func NewDingTalkAlarmStrategy(webhookUrl string) *DingTalkAlarmStrategy {
	return &DingTalkAlarmStrategy{
		webhookUrl: webhookUrl,
	}
}

// SendAlarm 发送告警
func (s *DingTalkAlarmStrategy) SendAlarm(message string) {
	// 实现钉钉告警逻辑
	println("[DingTalk] 发送告警: " + message)
	// 实际实现中需要调用钉钉机器人 API
}
