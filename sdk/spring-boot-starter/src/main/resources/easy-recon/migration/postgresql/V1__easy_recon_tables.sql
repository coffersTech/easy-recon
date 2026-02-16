-- PostgreSQL 建表脚本

-- 对账订单主记录
CREATE TABLE IF NOT EXISTS "easy_recon_order_main" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY,
  "order_no" VARCHAR(64) NOT NULL,
  "pay_amount" DECIMAL(18,2) NOT NULL,
  "platform_income" DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  "pay_fee" DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  "split_total_amount" DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  "pay_amount_fen" BIGINT,
  "platform_income_fen" BIGINT,
  "pay_fee_fen" BIGINT,
  "split_total_amount_fen" BIGINT,
  "refund_amount" DECIMAL(18,2) DEFAULT 0.00,
  "refund_amount_fen" BIGINT,
  "refund_status" SMALLINT DEFAULT 0,
  "refund_time" TIMESTAMP,
  "pay_status" SMALLINT DEFAULT 0,
  "split_status" SMALLINT DEFAULT 0,
  "notify_status" SMALLINT DEFAULT 0,
  "notify_result" TEXT,
  "recon_status" SMALLINT NOT NULL DEFAULT 0,
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "easy_recon_order_main" IS '对账订单主记录';
COMMENT ON COLUMN "easy_recon_order_main"."id" IS '主键 ID';
COMMENT ON COLUMN "easy_recon_order_main"."order_no" IS '订单号';
COMMENT ON COLUMN "easy_recon_order_main"."pay_amount" IS '实付金额';
COMMENT ON COLUMN "easy_recon_order_main"."platform_income" IS '平台收入';
COMMENT ON COLUMN "easy_recon_order_main"."pay_fee" IS '支付手续费';
COMMENT ON COLUMN "easy_recon_order_main"."split_total_amount" IS '分账总金额';
COMMENT ON COLUMN "easy_recon_order_main"."pay_amount_fen" IS '实付金额（分）';
COMMENT ON COLUMN "easy_recon_order_main"."platform_income_fen" IS '平台收入（分）';
COMMENT ON COLUMN "easy_recon_order_main"."pay_fee_fen" IS '支付手续费（分）';
COMMENT ON COLUMN "easy_recon_order_main"."split_total_amount_fen" IS '分账总金额（分）';
COMMENT ON COLUMN "easy_recon_order_main"."refund_amount" IS '退款金额';
COMMENT ON COLUMN "easy_recon_order_main"."refund_amount_fen" IS '退款金额（分）';
COMMENT ON COLUMN "easy_recon_order_main"."refund_status" IS '退款状态：0=未退款，1=部分退款，2=全额退款';
COMMENT ON COLUMN "easy_recon_order_main"."refund_time" IS '退款时间';
COMMENT ON COLUMN "easy_recon_order_main"."pay_status" IS '支付状态：0=处理中，1=成功，2=失败';
COMMENT ON COLUMN "easy_recon_order_main"."split_status" IS '分账状态：0=处理中，1=成功，2=失败';
COMMENT ON COLUMN "easy_recon_order_main"."notify_status" IS '通知状态：0=处理中，1=成功，2=失败';
COMMENT ON COLUMN "easy_recon_order_main"."notify_result" IS '最新通知结果';
COMMENT ON COLUMN "easy_recon_order_main"."recon_status" IS '对账状态：0=待对账，1=成功，2=失败';
COMMENT ON COLUMN "easy_recon_order_main"."create_time" IS '创建时间';
COMMENT ON COLUMN "easy_recon_order_main"."update_time" IS '更新时间';

-- 创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_no" ON "easy_recon_order_main" ("order_no");

-- 创建普通索引
CREATE INDEX IF NOT EXISTS "idx_main_recon_status" ON "easy_recon_order_main" ("recon_status");
CREATE INDEX IF NOT EXISTS "idx_main_create_time" ON "easy_recon_order_main" ("create_time");

-- 对账订单分账子记录
CREATE TABLE IF NOT EXISTS "easy_recon_order_split_sub" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY,
  "order_no" VARCHAR(64) NOT NULL,
  "sub_order_no" VARCHAR(64),
  "merchant_id" VARCHAR(64) NOT NULL,
  "merchant_order_no" VARCHAR(64),
  "split_amount" DECIMAL(18,2) NOT NULL,
  "split_amount_fen" BIGINT,
  "notify_status" SMALLINT NOT NULL DEFAULT 2,
  "notify_result" TEXT,
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "easy_recon_order_split_sub" IS '对账订单分账子记录';
COMMENT ON COLUMN "easy_recon_order_split_sub"."id" IS '主键 ID';
COMMENT ON COLUMN "easy_recon_order_split_sub"."order_no" IS '订单号';
COMMENT ON COLUMN "easy_recon_order_split_sub"."sub_order_no" IS '子订单号';
COMMENT ON COLUMN "easy_recon_order_split_sub"."merchant_id" IS '商户 ID';
COMMENT ON COLUMN "easy_recon_order_split_sub"."merchant_order_no" IS '商户原始订单号';
COMMENT ON COLUMN "easy_recon_order_split_sub"."split_amount" IS '分账金额';
COMMENT ON COLUMN "easy_recon_order_split_sub"."split_amount_fen" IS '分账金额（分）';
COMMENT ON COLUMN "easy_recon_order_split_sub"."notify_status" IS '通知状态 (0:失败, 1:成功, 2:待处理)';
COMMENT ON COLUMN "easy_recon_order_split_sub"."notify_result" IS '通知返回结果';
COMMENT ON COLUMN "easy_recon_order_split_sub"."create_time" IS '创建时间';
COMMENT ON COLUMN "easy_recon_order_split_sub"."update_time" IS '更新时间';

-- 对账订单退款分账子记录
CREATE TABLE IF NOT EXISTS "easy_recon_order_refund_split_sub" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY,
  "order_no" VARCHAR(64) NOT NULL,
  "sub_order_no" VARCHAR(64),
  "merchant_id" VARCHAR(64) NOT NULL,
  "merchant_order_no" VARCHAR(64),
  "refund_split_amount" DECIMAL(18,2) NOT NULL,
  "refund_split_amount_fen" BIGINT,
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "easy_recon_order_refund_split_sub" IS '对账订单退款分账子记录';
COMMENT ON COLUMN "easy_recon_order_refund_split_sub"."id" IS '主键 ID';
COMMENT ON COLUMN "easy_recon_order_refund_split_sub"."order_no" IS '订单号';
COMMENT ON COLUMN "easy_recon_order_refund_split_sub"."sub_order_no" IS '子订单号';
COMMENT ON COLUMN "easy_recon_order_refund_split_sub"."merchant_id" IS '商户 ID';
COMMENT ON COLUMN "easy_recon_order_refund_split_sub"."merchant_order_no" IS '商户原始订单号';
COMMENT ON COLUMN "easy_recon_order_refund_split_sub"."refund_split_amount" IS '退款分账金额';
COMMENT ON COLUMN "easy_recon_order_refund_split_sub"."refund_split_amount_fen" IS '退款分账金额（分）';
COMMENT ON COLUMN "easy_recon_order_refund_split_sub"."create_time" IS '创建时间';
COMMENT ON COLUMN "easy_recon_order_refund_split_sub"."update_time" IS '更新时间';

-- 创建普通索引
CREATE INDEX IF NOT EXISTS "idx_refund_sub_order_no" ON "easy_recon_order_refund_split_sub" ("order_no");
CREATE INDEX IF NOT EXISTS "idx_refund_sub_merchant_id" ON "easy_recon_order_refund_split_sub" ("merchant_id");
CREATE INDEX IF NOT EXISTS "idx_refund_sub_merchant_order_no" ON "easy_recon_order_refund_split_sub" ("merchant_id", "merchant_order_no");

-- 创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_mch_sub" ON "easy_recon_order_split_sub" ("order_no", "merchant_id", "sub_order_no");

-- 创建普通索引
CREATE INDEX IF NOT EXISTS "idx_sub_order_no" ON "easy_recon_order_split_sub" ("order_no");
CREATE INDEX IF NOT EXISTS "idx_sub_merchant_id" ON "easy_recon_order_split_sub" ("merchant_id");
CREATE INDEX IF NOT EXISTS "idx_sub_merchant_order_no" ON "easy_recon_order_split_sub" ("merchant_id", "merchant_order_no");

-- 对账异常记录
CREATE TABLE IF NOT EXISTS "easy_recon_exception" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY,
  "order_no" VARCHAR(64) NOT NULL,
  "merchant_id" VARCHAR(64) NOT NULL,
  "exception_msg" TEXT NOT NULL,
  "exception_step" SMALLINT NOT NULL,
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "easy_recon_exception" IS '对账异常记录';
COMMENT ON COLUMN "easy_recon_exception"."id" IS '主键 ID';
COMMENT ON COLUMN "easy_recon_exception"."order_no" IS '订单号';
COMMENT ON COLUMN "easy_recon_exception"."merchant_id" IS '商户 ID';
COMMENT ON COLUMN "easy_recon_exception"."exception_msg" IS '异常信息';
COMMENT ON COLUMN "easy_recon_exception"."exception_step" IS '异常步骤：1=支付状态，2=分账状态，3=通知状态，4=金额校验，5=其他';
COMMENT ON COLUMN "easy_recon_exception"."create_time" IS '创建时间';
COMMENT ON COLUMN "easy_recon_exception"."update_time" IS '更新时间';

-- 创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_no_exception" ON "easy_recon_exception" ("order_no");

-- 创建普通索引
CREATE INDEX IF NOT EXISTS "idx_exc_merchant_id" ON "easy_recon_exception" ("merchant_id");
CREATE INDEX IF NOT EXISTS "idx_exc_step" ON "easy_recon_exception" ("exception_step");
CREATE INDEX IF NOT EXISTS "idx_exc_create_time" ON "easy_recon_exception" ("create_time");

-- 对账通知日志
CREATE TABLE IF NOT EXISTS "easy_recon_notify_log" (
  "id" BIGSERIAL NOT NULL PRIMARY KEY,
  "order_no" VARCHAR(64) NOT NULL,
  "sub_order_no" VARCHAR(64),
  "merchant_id" VARCHAR(64) NOT NULL,
  "notify_url" VARCHAR(255) NOT NULL,
  "notify_status" SMALLINT NOT NULL DEFAULT 0,
  "notify_result" TEXT,
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "easy_recon_notify_log" IS '对账通知日志';
COMMENT ON COLUMN "easy_recon_notify_log"."id" IS '主键 ID';
COMMENT ON COLUMN "easy_recon_notify_log"."order_no" IS '订单号';
COMMENT ON COLUMN "easy_recon_notify_log"."sub_order_no" IS '子订单号';
COMMENT ON COLUMN "easy_recon_notify_log"."merchant_id" IS '商户 ID';
COMMENT ON COLUMN "easy_recon_notify_log"."notify_url" IS '通知 URL';
COMMENT ON COLUMN "easy_recon_notify_log"."notify_status" IS '通知状态：0=失败，1=成功';
COMMENT ON COLUMN "easy_recon_notify_log"."notify_result" IS '通知结果';
COMMENT ON COLUMN "easy_recon_notify_log"."create_time" IS '创建时间';
COMMENT ON COLUMN "easy_recon_notify_log"."update_time" IS '更新时间';

-- 创建唯一索引
CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_no_mch_sub_notify" ON "easy_recon_notify_log" ("order_no", "merchant_id", "sub_order_no");

-- 创建普通索引
CREATE INDEX IF NOT EXISTS "idx_notify_log_merchant_id" ON "easy_recon_notify_log" ("merchant_id");
CREATE INDEX IF NOT EXISTS "idx_notify_log_status" ON "easy_recon_notify_log" ("notify_status");
CREATE INDEX IF NOT EXISTS "idx_notify_log_create_time" ON "easy_recon_notify_log" ("create_time");

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

-- 为对账订单退款分账子记录创建触发器
CREATE TRIGGER update_easy_recon_order_refund_split_sub_modtime
    BEFORE UPDATE ON "easy_recon_order_refund_split_sub"
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
  "id" BIGSERIAL NOT NULL PRIMARY KEY,
  "rule_name" VARCHAR(128) NOT NULL,
  "rule_type" SMALLINT NOT NULL,
  "rule_expression" TEXT NOT NULL,
  "rule_desc" VARCHAR(255),
  "status" SMALLINT NOT NULL DEFAULT 1,
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "easy_recon_rule" IS '对账规则';
COMMENT ON COLUMN "easy_recon_rule"."id" IS '主键 ID';
COMMENT ON COLUMN "easy_recon_rule"."rule_name" IS '规则名称';
COMMENT ON COLUMN "easy_recon_rule"."rule_type" IS '规则类型：1=金额规则，2=状态规则，3=其他规则';
COMMENT ON COLUMN "easy_recon_rule"."rule_expression" IS '规则表达式';
COMMENT ON COLUMN "easy_recon_rule"."rule_desc" IS '规则描述';
COMMENT ON COLUMN "easy_recon_rule"."status" IS '状态：1=启用，0=禁用';
COMMENT ON COLUMN "easy_recon_rule"."create_time" IS '创建时间';
COMMENT ON COLUMN "easy_recon_rule"."update_time" IS '更新时间';

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
