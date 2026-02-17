import logging
import sys
import os
from datetime import datetime

# Add sdk to path
sys.path.append(os.path.join(os.path.dirname(__file__), "../sdk/python"))

from config.recon_config import ReconConfig
from core.easy_recon_factory import EasyReconFactory
from entity.recon_order_split_sub import ReconOrderSplitSub

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("IntegrationTest")

def main():
    logger.info("Starting integration test...")

    # 1. Config (Assuming local DB is running, or mock it)
    # WARNING: This test requires a running MySQL/PostgreSQL 
    # For now, we print what would happen if we don't have a DB credentials
    config = ReconConfig(
        db_host="localhost",
        db_port=3306,
        db_user="root",
        db_password="password",
        db_name="easy_recon_test"
    )
    
    logger.info("Initializing SDK...")
    try:
        sdk = EasyReconFactory.create(config)
    except Exception as e:
        logger.error(f"Failed to init SDK (Expected if no DB): {e}")
        return

    # 2. Simulate Pay Success
    order_no = f"ORD_{int(datetime.now().timestamp())}"
    logger.info(f"Processing Order: {order_no}")

    split_subs = [
        ReconOrderSplitSub(
            sub_order_no=f"{order_no}_SUB1",
            merchant_id="M001",
            split_amount=10.0,
            notify_status=0
        ),
        ReconOrderSplitSub(
            sub_order_no=f"{order_no}_SUB2",
            merchant_id="M002",
            split_amount=5.0,
            notify_status=0
        )
    ]

    # Recon Order
    # Pay: 15.0, Platform: 0, Fee: 0
    # Expected: PENDING (because notify_status=0)
    result = sdk.recon_order(
        order_no, 15.0, 0.0, 0.0, split_subs,
        pay_status=1, # Success
        split_status=1, # Success
        notify_status=0 # Processing
    )
    logger.info(f"Recon Order Result: {result}")
    
    # Verify Status
    order_main = sdk.get_order_main(order_no)
    if order_main:
        logger.info(f"Order Status: {order_main.recon_status}") # Should be 0 (PENDING)
    
    # 3. Simulate Notify
    logger.info("Simulating Notification...")
    sdk.recon_notify(order_no, "M001", "http://notify.com", 1, "Success")
    sdk.recon_notify(order_no, "M002", "http://notify.com", 1, "Success")
    
    # Check if status updated to SUCCESS (1)
    # Note: Logic in recon_notify calls retry_recon, which checks if all are notified
    order_main = sdk.get_order_main(order_no)
    if order_main:
        logger.info(f"Order Status After Notify: {order_main.recon_status}") # Should be 1 (SUCCESS)

if __name__ == "__main__":
    main()
