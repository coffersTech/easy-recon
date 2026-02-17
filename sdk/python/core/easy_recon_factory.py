from config.recon_config import ReconConfig
from repository.recon_repository import ReconRepository
from service.alarm_service import AlarmService
from service.exception_record_service import ExceptionRecordService
from service.realtime_recon_service import RealtimeReconService
from service.timing_recon_service import TimingReconService
from core.easy_recon_api import EasyReconApi

class EasyReconFactory:
    """
    Factory to create EasyReconApi instance
    """
    @staticmethod
    def create(config: ReconConfig) -> EasyReconApi:
        recon_repository = ReconRepository(config)
        alarm_service = AlarmService()
        exception_record_service = ExceptionRecordService(recon_repository)
        
        realtime_service = RealtimeReconService(
            config, recon_repository, exception_record_service, alarm_service
        )
        timing_service = TimingReconService(recon_repository)
        
        return EasyReconApi(realtime_service, timing_service, recon_repository)
