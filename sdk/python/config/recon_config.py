from dataclasses import dataclass
from typing import Optional

@dataclass
class ReconConfig:
    """
    Easy Recon SDK Configuration
    """
    # Database Configuration
    db_host: str = "localhost"
    db_port: int = 3306
    db_user: str = "root"
    db_password: str = ""
    db_name: str = "easy_recon"
    db_type: str = "mysql" # mysql or postgresql
    # table_prefix removed as per requirement
    auto_create_table: bool = True
    
    # Connection Pool Configuration
    pool_name: str = "easy_recon_pool"
    pool_size: int = 5
    
    # Business Logic Configuration
    amount_tolerance: float = 0.01

    def __post_init__(self):
        if not self.db_host:
            raise ValueError("db_host cannot be empty")
        if not self.db_user:
            raise ValueError("db_user cannot be empty")
