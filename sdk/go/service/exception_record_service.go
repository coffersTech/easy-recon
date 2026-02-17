package service

import (
	"log"
	"time"

	"github.com/coffersTech/easy-recon/sdk/go/entity"
	"github.com/coffersTech/easy-recon/sdk/go/repository"
)

// ExceptionRecordService 异常记录服务
type ExceptionRecordService struct {
	reconRepository repository.ReconRepository
}

// NewExceptionRecordService 创建异常记录服务
func NewExceptionRecordService(reconRepository repository.ReconRepository) *ExceptionRecordService {
	return &ExceptionRecordService{
		reconRepository: reconRepository,
	}
}

// RecordReconException 记录对账异常
func (s *ExceptionRecordService) RecordReconException(orderNo, merchantId, msg string, step int) {
	exception := &entity.ReconException{
		OrderNo:       orderNo,
		MerchantId:    merchantId,
		ExceptionType: 1, // 假设 1 为业务异常
		ExceptionMsg:  msg,
		ExceptionStep: step,
		CreateTime:    time.Now(),
		UpdateTime:    time.Now(),
	}

	_, err := s.reconRepository.SaveException(exception)
	if err != nil {
		log.Printf("Failed to save exception record: %v", err)
	}
}
