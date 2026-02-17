package service

import (
	"bytes"
	"encoding/json"
	"fmt"
	"log"
	"net/http"

	"github.com/coffersTech/easy-recon/sdk/go/config"
)

// AlarmService 告警服务
type AlarmService struct {
	config *config.ReconConfig
}

// NewAlarmService 创建告警服务
func NewAlarmService(cfg *config.ReconConfig) *AlarmService {
	return &AlarmService{
		config: cfg,
	}
}

// SendReconAlarm 发送对账告警
func (s *AlarmService) SendReconAlarm(orderNo, merchantId, msg string) {
	content := fmt.Sprintf("EasyRecon Alarm: OrderNo=%s, MerchantId=%s, Msg=%s", orderNo, merchantId, msg)
	s.SendAlarm(content)
}

// SendAlarm 发送告警
func (s *AlarmService) SendAlarm(content string) {
	// Log alarm
	log.Printf("[ALARM] %s", content)

	// DingTalk alarm
	if s.config.EasyRecon.Alarm.Type == "dingtalk" && s.config.EasyRecon.Alarm.DingTalk.WebhookUrl != "" {
		s.sendDingTalkAlarm(content)
	}
}

func (s *AlarmService) sendDingTalkAlarm(content string) {
	msg := map[string]interface{}{
		"msgtype": "text",
		"text": map[string]string{
			"content": content,
		},
	}

	payload, _ := json.Marshal(msg)
	resp, err := http.Post(s.config.EasyRecon.Alarm.DingTalk.WebhookUrl, "application/json", bytes.NewBuffer(payload))
	if err != nil {
		log.Printf("Failed to send DingTalk alarm: %v", err)
		return
	}
	defer resp.Body.Close()
}
