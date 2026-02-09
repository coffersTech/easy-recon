import sys
import os
from datetime import datetime

# Add SDK path to sys.path to allow importing modules
sdk_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../sdk/python'))
sys.path.append(sdk_path)

from service.realtime_recon_service import RealtimeReconService
from entity.recon_order_main import ReconOrderMain
from entity.recon_order_split_sub import ReconOrderSplitSub

# Mock Repository
class MockReconRepository:
    def save_order_main(self, order_main):
        print(f"Saving Order Main: {order_main.__dict__}")
        return True

    def batch_save_order_split_sub(self, split_subs):
        print(f"Batch Saving Order Split Subs: {[sub.__dict__ for sub in split_subs]}")
        return True

    def update_recon_status(self, order_no, status):
        print(f"Updating Recon Status for {order_no} to {status}")
        return True

# Mock Alarm Service
class MockAlarmService:
    def send_alarm(self, message):
        print(f"Sending Alarm: {message}")

def run_demo():
    print("--- Starting Python Easy Recon SDK Demo ---")
    
    # Initialize Services
    repo = MockReconRepository()
    alarm = MockAlarmService()
    service = RealtimeReconService(repo, alarm)

    # Create Mock Data
    order_main = ReconOrderMain()
    order_main.order_no = "ORD-PY-123456"
    order_main.amount = 150.00
    order_main.merchant_id = "MCH-PY-001"
    order_main.transaction_time = datetime.now()

    sub1 = ReconOrderSplitSub()
    sub1.sub_order_no = "SUB-PY-001"
    sub1.amount = 100.00
    
    sub2 = ReconOrderSplitSub()
    sub2.sub_order_no = "SUB-PY-002"
    sub2.amount = 50.00

    split_subs = [sub1, sub2]

    # Execute Recon
    try:
        result = service.do_realtime_recon(order_main, split_subs)
        if result:
            print("--- Recon Successful ---")
        else:
            print("--- Recon Failed ---")
    except Exception as e:
        print(f"Error executing demo: {e}")

if __name__ == "__main__":
    run_demo()
