import psycopg2
import sys

def drop_tables():
    config = {
        "host": "localhost",
        "port": 5432,
        "user": "postgresql",
        "password": "postgresql",
        "database": "easy_recon_demo"
    }
    
    try:
        conn = psycopg2.connect(**config)
        cursor = conn.cursor()
        
        tables = [
            "easy_recon_order_main",
            "easy_recon_order_split_sub",
            "easy_recon_order_refund_split_sub",
            "easy_recon_exception",
            "easy_recon_notify_log",
            # Also drop legacy tables if they exist to be clean
            "recon_order_main",
            "recon_order_split_sub",
            "recon_order_refund_split_sub",
            "recon_exception",
            "recon_notify_log"
        ]
        
        for table in tables:
            cursor.execute(f"DROP TABLE IF EXISTS {table}")
            print(f"Dropped table: {table}")
            
        conn.commit()
        cursor.close()
        conn.close()
        print("All tables dropped successfully.")
    except Exception as e:
        print(f"Error dropping tables: {e}")
        sys.exit(1)

if __name__ == "__main__":
    drop_tables()
