-- PostgreSQL 建表脚本

-- 对账订单主记录
CREATE TABLE IF NOT EXISTS "easy_recon_order_main" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY COMMENT '主键 ID',
  "order_no" VARCHAR(64) NOT NULL COMMENT '订单号',
  "merchant_id" VARCHAR(64) NOT NULL COMMENT '商户 ID',
  "pay_amount" DECIMAL(18,2) NOT NULL COMMENT '实付金额',
  "platform_income" DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '平台收入',
  "pay_fee" DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '支付手续费',
  "split_total_amount" DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '分账总金额',
  "recon_status" SMALLINT NOT NULL DEFAULT 0 COMMENT '对账状态：0=待对账，1=成功，2=失败',
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_no" ON "easy_recon_order_main" ("order_no");

-- 创建普通索引
CREATE INDEX IF NOT EXISTS "idx_merchant_id" ON "easy_recon_order_main" ("merchant_id");
CREATE INDEX IF NOT EXISTS "idx_recon_status" ON "easy_recon_order_main" ("recon_status");
CREATE INDEX IF NOT EXISTS "idx_create_time" ON "easy_recon_order_main" ("create_time");

-- 对账订单分账子记录
CREATE TABLE IF NOT EXISTS "easy_recon_order_split_sub" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY COMMENT '主键 ID',
  "order_no" VARCHAR(64) NOT NULL COMMENT '订单号',
  "merchant_id" VARCHAR(64) NOT NULL COMMENT '商户 ID',
  "split_amount" DECIMAL(18,2) NOT NULL COMMENT '分账金额',
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_merchant" ON "easy_recon_order_split_sub" ("order_no", "merchant_id");

-- 创建普通索引
CREATE INDEX IF NOT EXISTS "idx_order_no" ON "easy_recon_order_split_sub" ("order_no");
CREATE INDEX IF NOT EXISTS "idx_merchant_id_sub" ON "easy_recon_order_split_sub" ("merchant_id");

-- 对账异常记录
CREATE TABLE IF NOT EXISTS "easy_recon_exception" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY COMMENT '主键 ID',
  "order_no" VARCHAR(64) NOT NULL COMMENT '订单号',
  "merchant_id" VARCHAR(64) NOT NULL COMMENT '商户 ID',
  "exception_msg" TEXT NOT NULL COMMENT '异常信息',
  "exception_step" SMALLINT NOT NULL COMMENT '异常步骤：1=支付状态，2=分账状态，3=通知状态，4=金额校验，5=其他',
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_no_exception" ON "easy_recon_exception" ("order_no");

-- 创建普通索引
CREATE INDEX IF NOT EXISTS "idx_merchant_id_exception" ON "easy_recon_exception" ("merchant_id");
CREATE INDEX IF NOT EXISTS "idx_exception_step" ON "easy_recon_exception" ("exception_step");
CREATE INDEX IF NOT EXISTS "idx_create_time_exception" ON "easy_recon_exception" ("create_time");

-- 对账通知日志
CREATE TABLE IF NOT EXISTS "easy_recon_notify_log" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY COMMENT '主键 ID',
  "order_no" VARCHAR(64) NOT NULL COMMENT '订单号',
  "merchant_id" VARCHAR(64) NOT NULL COMMENT '商户 ID',
  "notify_url" VARCHAR(255) NOT NULL COMMENT '通知 URL',
  "notify_status" SMALLINT NOT NULL DEFAULT 0 COMMENT '通知状态：0=失败，1=成功',
  "notify_result" TEXT COMMENT '通知结果',
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_no_notify" ON "easy_recon_notify_log" ("order_no");

-- 创建普通索引
CREATE INDEX IF NOT EXISTS "idx_merchant_id_notify" ON "easy_recon_notify_log" ("merchant_id");
CREATE INDEX IF NOT EXISTS "idx_notify_status" ON "easy_recon_notify_log" ("notify_status");
CREATE INDEX IF NOT EXISTS "idx_create_time_notify" ON "easy_recon_notify_log" ("create_time");

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 为对账订单主记录创建触发器
CREATE TRIGGER update_easy_recon_order_main_modtime
    BEFORE UPDATE ON "easy_recon_order_main"
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- 为对账订单分账子记录创建触发器
CREATE TRIGGER update_easy_recon_order_split_sub_modtime
    BEFORE UPDATE ON "easy_recon_order_split_sub"
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- 为对账异常记录创建触发器
CREATE TRIGGER update_easy_recon_exception_modtime
    BEFORE UPDATE ON "easy_recon_exception"
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- 为对账通知日志创建触发器
CREATE TRIGGER update_easy_recon_notify_log_modtime
    BEFORE UPDATE ON "easy_recon_notify_log"
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- 对账规则表
CREATE TABLE IF NOT EXISTS "easy_recon_rule" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY COMMENT '主键 ID',
  "rule_name" VARCHAR(128) NOT NULL COMMENT '规则名称',
  "rule_type" SMALLINT NOT NULL COMMENT '规则类型：1=金额规则，2=状态规则，3=其他规则',
  "rule_expression" TEXT NOT NULL COMMENT '规则表达式',
  "rule_desc" VARCHAR(255) COMMENT '规则描述',
  "status" SMALLINT NOT NULL DEFAULT 1 COMMENT '状态：1=启用，0=禁用',
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间'
);

-- 创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS "uk_rule_name" ON "easy_recon_rule" ("rule_name");

-- 创建普通索引
CREATE INDEX IF NOT EXISTS "idx_rule_type" ON "easy_recon_rule" ("rule_type");
CREATE INDEX IF NOT EXISTS "idx_rule_status" ON "easy_recon_rule" ("status");
CREATE INDEX IF NOT EXISTS "idx_rule_create_time" ON "easy_recon_rule" ("create_time");

-- 为对账规则表创建触发器
CREATE TRIGGER update_easy_recon_rule_modtime
    BEFORE UPDATE ON "easy_recon_rule"
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- 插入默认对账规则
INSERT INTO "easy_recon_rule" ("rule_name", "rule_type", "rule_expression", "rule_desc", "status", "create_time", "update_time")
VALUES 
('默认金额规则', 1, 'payAmount = splitTotalAmount + platformIncome + payFee', '默认金额等式校验规则', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('默认状态规则', 2, 'payStatus && notifyStatus', '默认状态校验规则', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT ("rule_name") DO NOTHING;
