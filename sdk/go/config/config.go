package config

import (
	"os"

	"github.com/shopspring/decimal"
	"gopkg.in/yaml.v3"
)

// ReconConfig matches ReconSdkProperties in Java
type ReconConfig struct {
	EasyRecon struct {
		Enabled         bool            `yaml:"enabled"`
		AutoInitTables  bool            `yaml:"auto-init-tables"`
		AmountTolerance decimal.Decimal `yaml:"amount-tolerance"`
		BatchSize       int             `yaml:"batch-size"`
		Timing          TimingConfig    `yaml:"timing"`
		Alarm           AlarmConfig     `yaml:"alarm"`
	} `yaml:"easy-recon"`
}

type TimingConfig struct {
	Enabled bool `yaml:"enabled"`
}

type AlarmConfig struct {
	Type     string         `yaml:"type"`
	DingTalk DingTalkConfig `yaml:"dingtalk"`
}

type DingTalkConfig struct {
	WebhookUrl string `yaml:"webhook-url"`
}

// DefaultConfig returns the default configuration
func DefaultConfig() *ReconConfig {
	cfg := &ReconConfig{}
	cfg.EasyRecon.Enabled = true
	cfg.EasyRecon.AutoInitTables = true
	cfg.EasyRecon.AmountTolerance = decimal.NewFromFloat(0.01)
	cfg.EasyRecon.BatchSize = 1000
	cfg.EasyRecon.Timing.Enabled = false
	cfg.EasyRecon.Alarm.Type = "log"
	return cfg
}

// LoadConfig loads configuration from a YAML file
func LoadConfig(path string) (*ReconConfig, error) {
	config := DefaultConfig()

	if path == "" {
		return config, nil
	}

	data, err := os.ReadFile(path)
	if err != nil {
		return nil, err
	}

	err = yaml.Unmarshal(data, config)
	if err != nil {
		return nil, err
	}

	return config, nil
}
