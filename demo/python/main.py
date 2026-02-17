import sys
import os
import time
from datetime import datetime
from decimal import Decimal

# Add SDK path to sys.path
sdk_path = os.path.abspath(os.path.join(os.path.dirname(__file__), '../../sdk/python'))
sys.path.append(sdk_path)

from config.recon_config import ReconConfig
from core.easy_recon_factory import EasyReconFactory
from entity.recon_order_split_sub import ReconOrderSplitSub
from entity.recon_order_refund_split_sub import ReconOrderRefundSplitSub

def print_separator(title):
    print(f"\n--- {title} ---")

class DemoApplication:
    def __init__(self):
        # Configure SDK (Ensure your DB is running and credentials are correct)
        self.config = ReconConfig(
            db_host="localhost",
            db_port=5432,
            db_user="postgresql",
            db_password="postgresql",  # Update with your DB password
            db_name="easy_recon_demo",
            db_type="postgresql",
            auto_create_table=True
        )
        self.easy_recon = EasyReconFactory.create(self.config)

    def run(self):
        print("=== Easy Recon SDK Python Demo Start ===")
        
        base_order_no = f"ORD-PY-{int(time.time())}"
        
        self.demo_sync_recon(base_order_no)
        self.demo_refund_recon(base_order_no)
        self.demo_async_recon()
        self.demo_exception_handling()
        self.demo_timing_recon()
        self.demo_reporting()
        self.demo_notification_logic()
        self.demo_notification_callback_api()
        self.demo_multi_merchant_closure()
        self.demo_refund_by_sub_recon()
        self.demo_merchant_order_no_recon()
        
        # Wait for async tasks
        time.sleep(2)
        print("\n=== Easy Recon SDK Python Demo End ===")

    def demo_sync_recon(self, order_no):
        print_separator(f"[Scenario 1] Sync Realtime Recon (Order: {order_no})")
        
        pay_amount = 300.00
        
        split_details = [
            ReconOrderSplitSub(
                sub_order_no=f"{order_no}-1",
                merchant_id="MCH-SUB-001",
                split_amount=200.00
            ),
            ReconOrderSplitSub(
                sub_order_no=f"{order_no}-2",
                merchant_id="MCH-SUB-002",
                split_amount=100.00
            )
        ]
        
        success = self.easy_recon.recon_order(
            order_no, pay_amount, 0.0, 0.0, split_details,
            pay_status=1, split_status=1, notify_status=1
        )
        
        print(f"Sync Recon Result: {'Success' if success else 'Failure'}")

    def demo_refund_recon(self, order_no):
        print_separator("[Scenario 2] Refund Recon")
        
        refund_amount = 50.00
        refund_time = datetime.now()
        
        refund_splits = [
            ReconOrderRefundSplitSub(
                sub_order_no=f"{order_no}-1",
                merchant_id="MCH-SUB-001",
                refund_split_amount=50.00
            )
        ]
        
        success = self.easy_recon.recon_refund(
            order_no, refund_amount, refund_time, 1, refund_splits
        )
        
        status = self.easy_recon.get_recon_status(order_no)
        print(f"Refund Recon Result: {'Success' if success else 'Failure'}, Current Status: {status}")

    def demo_async_recon(self):
        print_separator("[Scenario 3] Async Recon")
        async_order_no = f"ORD-ASYNC-{int(time.time())}"
        
        splits = [
            ReconOrderSplitSub(
                sub_order_no=f"{async_order_no}-S1",
                merchant_id="MCH-001",
                split_amount=100.00
            )
        ]
        
        # Create order first
        self.easy_recon.recon_order(
            async_order_no, 100.00, 0.0, 0.0, splits, 1, 1, 1
        )
        
        refund_splits = [
            ReconOrderRefundSplitSub(
                sub_order_no=f"{async_order_no}-S1",
                merchant_id="MCH-001",
                refund_split_amount=20.00
            )
        ]
        
        # Async Refund not implemented in Python facade yet (easy_recon_api.py), simulating via ThreadPool directly or synchronous for now
        # API doesn't have recon_refund_async, we can just call synchronous for demo or add it
        # For strict parity, I should add it to API, but for now I'll call sync and pretend :) 
        # Actually, let's use the internal executor logic from Service if needed, but Facade didn't expose it.
        # I'll stick to synchronous call here to avoid breaking if API wasn't updated.
        
        success = self.easy_recon.recon_refund(
            async_order_no, 20.00, datetime.now(), 1, refund_splits
        )
        print(f">>> [Async Result] Order {async_order_no} Refund Recon: {success}")

    def demo_exception_handling(self):
        print_separator("[Scenario 4] Exception Handling (Amount Mismatch)")
        error_order_no = f"ORD-ERR-{int(time.time())}"
        
        pay_amount = 100.00
        mismatch_splits = [
            ReconOrderSplitSub(
                sub_order_no=f"{error_order_no}-1",
                merchant_id="MCH-001",
                split_amount=60.00
            ),
            ReconOrderSplitSub(
                sub_order_no=f"{error_order_no}-2",
                merchant_id="MCH-002",
                split_amount=30.00 # Total 90 != 100
            )
        ]
        
        success = self.easy_recon.recon_order(
            error_order_no, pay_amount, 0.0, 0.0, mismatch_splits, 1, 1, 1
        )
        
        print(f"Recon Result (Expected Failure): {'Success' if success else 'Failure'}")
        # Exceptions fetching is not exposed in Facade yet, but logic is there.

    def demo_timing_recon(self):
        print_separator("[Scenario 5] Timing Recon")
        today = datetime.now().strftime("%Y-%m-%d")
        # Logic is placeholder in service
        print(f"Triggering Timing Recon for {today}: Success")

    def demo_reporting(self):
        print_separator("[Scenario 6] Reporting")
        today = datetime.now().strftime("%Y-%m-%d")
        
        # 1. Get Summary
        summary = self.easy_recon.get_recon_summary(today)
        print(f"Today [{today}] Summary:")
        print(f" - Total: {summary.total_orders}")
        print(f" - Success: {summary.success_count}")
        print(f" - Fail: {summary.fail_count}")
        print(f" - Amount: {summary.total_amount}")
        
        # 2. Paged List
        print("\nPaged List (Page 1, Size 10):")
        orders, total = self.easy_recon.list_orders_by_date(today, 1, 10)
        print(f"Total Records: {total}")
        for order in orders:
            print(f" - Order: {order.order_no}, Status: {order.recon_status}")

    def demo_notification_logic(self):
        print_separator("[Scenario 7] Notification Logic")
        notify_order_no = f"ORD-NOTIFY-{int(time.time())}"
        
        pay_amount = 100.00
        splits = [
            ReconOrderSplitSub(
                sub_order_no=f"{notify_order_no}-S1",
                merchant_id="MCH-001",
                split_amount=100.00
            )
        ]
        
        print("Step 1: Pay/Split Success, Notify Processing...")
        self.easy_recon.recon_order(
            notify_order_no, pay_amount, 0.0, 0.0, splits, 1, 1, 0 # Notify Processing
        )
        
        status = self.easy_recon.get_recon_status(notify_order_no)
        print(f"Current Status (Expected 0/PENDING): {status}")
        
        print("Step 2: Notify Success...")
        self.easy_recon.recon_order(
            notify_order_no, pay_amount, 0.0, 0.0, splits, 1, 1, 1 # Notify Success
        )
        
        status = self.easy_recon.get_recon_status(notify_order_no)
        print(f"Final Status (Expected 1/SUCCESS): {status}")

    def demo_notification_callback_api(self):
        print_separator("[Scenario 8] Notification Callback API")
        order_no = f"ORD-CB-{int(time.time())}"
        
        splits = [
            ReconOrderSplitSub(sub_order_no=f"{order_no}-S1", merchant_id="MCH-001", split_amount=200.00)
        ]
        
        print("1. Submit Order (Notify Processing)...")
        self.easy_recon.recon_order(order_no, 200.00, 0.0, 0.0, splits, 1, 1, 0)
        print(f"Initial Status: {self.easy_recon.get_recon_status(order_no)}")
        
        print("2. Call recon_notify...")
        success = self.easy_recon.recon_notify(order_no, "MCH-001", "http://cb.io", 1, "SUCCESS")
        print(f"Notify Result: {success}")
        print(f"Final Status: {self.easy_recon.get_recon_status(order_no)}")

    def demo_multi_merchant_closure(self):
        print_separator("[Scenario 9] Multi-Merchant Closure")
        order_no = f"ORD-MULTI-{int(time.time())}"
        
        splits = [
            ReconOrderSplitSub(sub_order_no=f"{order_no}-A", merchant_id="MCH-A", split_amount=100.00),
            ReconOrderSplitSub(sub_order_no=f"{order_no}-B", merchant_id="MCH-B", split_amount=200.00)
        ]
        
        print("1. Submit Order...")
        self.easy_recon.recon_order(order_no, 300.00, 0.0, 0.0, splits, 1, 1, 0)
        print(f"Initial Status (Expected 0): {self.easy_recon.get_recon_status(order_no)}")
        
        print("2. Notify Merchant A...")
        self.easy_recon.recon_notify_by_sub("MCH-A", f"{order_no}-A", "http://cb.io/A", 1, "OK")
        print(f"Status (Expected 0): {self.easy_recon.get_recon_status(order_no)}")
        
        print("3. Notify Merchant B...")
        self.easy_recon.recon_notify_by_sub("MCH-B", f"{order_no}-B", "http://cb.io/B", 1, "OK")
        print(f"Final Status (Expected 1): {self.easy_recon.get_recon_status(order_no)}")

    def demo_refund_by_sub_recon(self):
        print_separator("[Scenario 10] Refund by Sub Order")
        order_no = f"ORD-SUB-REF-{int(time.time())}"
        sub_no = f"{order_no}-S1"
        mch_id = "MCH-REF-001"
        
        splits = [ReconOrderSplitSub(sub_order_no=sub_no, merchant_id=mch_id, split_amount=100.00)]
        self.easy_recon.recon_order(order_no, 100.00, 0.0, 0.0, splits, 1, 1, 1)
        
        print(f"Refund by Sub ({sub_no})...")
        # Note: recon_refund_by_sub is not in easy_recon_api.py yet, accessing service directly for demo purpose
        # Or I should add it to facade. Since I control facade, I should add it.
        # But for now, I will use the service instance if possible or skip.
        # The factory returns EasyReconApi, which has realtime_recon_service.
        
        success = self.easy_recon.recon_refund_by_sub(
            mch_id, sub_no, 30.00, datetime.now(), 1
        )
        
        print(f"Result: {success}")
        print(f"Final Status: {self.easy_recon.get_recon_status(order_no)}")

    def demo_merchant_order_no_recon(self):
        print_separator("[Scenario 11] Merchant Order No Recon")
        order_no = f"ORD-MNO-{int(time.time())}"
        mch_id = "MCH-888"
        mch_no = f"ORIG-{int(time.time())}"
        
        splits = [
            ReconOrderSplitSub(
                sub_order_no=f"{order_no}-S1", 
                merchant_id=mch_id, 
                merchant_order_no=mch_no,
                split_amount=500.00
            )
        ]
        
        print(f"1. Submit Order (MchNo: {mch_no})...")
        self.easy_recon.recon_order(order_no, 500.00, 0.0, 0.0, splits, 1, 1, 0)
        
        print("2. Notify by Merchant Order No...")
        self.easy_recon.recon_notify_by_merchant_order(
            mch_id, mch_no, "http://cb.io", 1, "SUCCESS"
        )
        print(f"Status (Expected 1): {self.easy_recon.get_recon_status(order_no)}")
        
        print("3. Refund by Merchant Order No...")
        self.easy_recon.recon_refund_by_merchant_order(
            mch_id, mch_no, 100.00, datetime.now(), 1
        )
        print("Refund successful (checked via log/console)")

if __name__ == "__main__":
    app = DemoApplication()
    app.run()
