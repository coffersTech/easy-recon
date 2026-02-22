-- Easy Recon PostgreSQL 初始化全量脚本 (适用于 1.0.1+ 版本全量部署)
-- 包含：主订单、业务子单(意图)、分账明细(事实)、退款明细、商户结算统计、异常记录及对账规则

-- 1. 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. 对账订单主记录
CREATE TABLE IF NOT EXISTS "easy_recon_order_main" (
  "id" BIGSERIAL PRIMARY KEY,
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

CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_no" ON "easy_recon_order_main" ("order_no");
CREATE INDEX IF NOT EXISTS "idx_main_recon_status" ON "easy_recon_order_main" ("recon_status");
CREATE INDEX IF NOT EXISTS "idx_main_create_time" ON "easy_recon_order_main" ("create_time");

CREATE TRIGGER update_easy_recon_order_main_modtime
    BEFORE UPDATE ON "easy_recon_order_main"
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- 3. 业务子订单表 (意图层)
CREATE TABLE IF NOT EXISTS "easy_recon_order_sub" (
  "id" BIGSERIAL PRIMARY KEY,
  "order_no" VARCHAR(64) NOT NULL,
  "sub_order_no" VARCHAR(64),
  "merchant_order_no" VARCHAR(64),
  "merchant_id" VARCHAR(64) NOT NULL,
  "order_amount" DECIMAL(18,2),
  "order_amount_fen" BIGINT,
  "split_amount" DECIMAL(18,2),
  "split_amount_fen" BIGINT,
  "fee" DECIMAL(18,2),
  "fee_fen" BIGINT,
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "easy_recon_order_sub" IS '业务子订单表';
COMMENT ON COLUMN "easy_recon_order_sub"."id" IS '主键 ID';
COMMENT ON COLUMN "easy_recon_order_sub"."order_no" IS '主订单号';
COMMENT ON COLUMN "easy_recon_order_sub"."sub_order_no" IS '业务子订单号';
COMMENT ON COLUMN "easy_recon_order_sub"."merchant_order_no" IS '商户原始订单号';
COMMENT ON COLUMN "easy_recon_order_sub"."merchant_id" IS '商户 ID';
COMMENT ON COLUMN "easy_recon_order_sub"."order_amount" IS '订单金额';
COMMENT ON COLUMN "easy_recon_order_sub"."order_amount_fen" IS '订单金额（分）';
COMMENT ON COLUMN "easy_recon_order_sub"."split_amount" IS '分账金额';
COMMENT ON COLUMN "easy_recon_order_sub"."split_amount_fen" IS '分账金额（分）';
COMMENT ON COLUMN "easy_recon_order_sub"."fee" IS '手续费';
COMMENT ON COLUMN "easy_recon_order_sub"."fee_fen" IS '手续费（分）';
COMMENT ON COLUMN "easy_recon_order_sub"."create_time" IS '创建时间';
COMMENT ON COLUMN "easy_recon_order_sub"."update_time" IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_mch_sub" ON "easy_recon_order_sub" ("order_no", "merchant_id", "sub_order_no");
CREATE INDEX IF NOT EXISTS "idx_sub_order_no" ON "easy_recon_order_sub" ("order_no");
CREATE INDEX IF NOT EXISTS "idx_sub_merchant_id" ON "easy_recon_order_sub" ("merchant_id");

CREATE TRIGGER update_easy_recon_order_sub_modtime
    BEFORE UPDATE ON "easy_recon_order_sub"
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- 4. 分账事实明细表 (事实层)
CREATE TABLE IF NOT EXISTS "easy_recon_order_split_detail" (
  "id" BIGSERIAL PRIMARY KEY,
  "order_no" VARCHAR(64) NOT NULL,
  "merchant_id" VARCHAR(64) NOT NULL,
  "split_amount" DECIMAL(18,2) NOT NULL,
  "split_amount_fen" BIGINT,
  "arrival_amount" DECIMAL(18,2) DEFAULT 0.00,
  "arrival_amount_fen" BIGINT,
  "split_fee" DECIMAL(18,2) DEFAULT 0.00,
  "split_fee_fen" BIGINT,
  "notify_status" SMALLINT NOT NULL DEFAULT 2,
  "notify_result" TEXT,
  "settlement_type" SMALLINT,
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "easy_recon_order_split_detail" IS '分账事实明细表';
COMMENT ON COLUMN "easy_recon_order_split_detail"."id" IS '主键 ID';
COMMENT ON COLUMN "easy_recon_order_split_detail"."order_no" IS '订单号';
COMMENT ON COLUMN "easy_recon_order_split_detail"."merchant_id" IS '商户 ID';
COMMENT ON COLUMN "easy_recon_order_split_detail"."split_amount" IS '分账金额';
COMMENT ON COLUMN "easy_recon_order_split_detail"."split_amount_fen" IS '分账金额（分）';
COMMENT ON COLUMN "easy_recon_order_split_detail"."arrival_amount" IS '实际到账金额 (元)';
COMMENT ON COLUMN "easy_recon_order_split_detail"."arrival_amount_fen" IS '实际到账金额 (分)';
COMMENT ON COLUMN "easy_recon_order_split_detail"."split_fee" IS '分账手续费 (元)';
COMMENT ON COLUMN "easy_recon_order_split_detail"."split_fee_fen" IS '分账手续费 (分)';
COMMENT ON COLUMN "easy_recon_order_split_detail"."notify_status" IS '通知状态 (0:失败, 1:成功, 2:待处理)';
COMMENT ON COLUMN "easy_recon_order_split_detail"."notify_result" IS '通知返回结果';
COMMENT ON COLUMN "easy_recon_order_split_detail"."settlement_type" IS '判定后的清算类型';
COMMENT ON COLUMN "easy_recon_order_split_detail"."create_time" IS '创建时间';
COMMENT ON COLUMN "easy_recon_order_split_detail"."update_time" IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_mch_split" ON "easy_recon_order_split_detail" ("order_no", "merchant_id");
CREATE INDEX IF NOT EXISTS "idx_split_order_no" ON "easy_recon_order_split_detail" ("order_no");

CREATE TRIGGER update_easy_recon_order_split_detail_modtime
    BEFORE UPDATE ON "easy_recon_order_split_detail"
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- 5. 退款事实明细表 (事实层)
CREATE TABLE IF NOT EXISTS "easy_recon_order_refund_detail" (
  "id" BIGSERIAL PRIMARY KEY,
  "order_no" VARCHAR(64) NOT NULL,
  "merchant_id" VARCHAR(64) NOT NULL,
  "refund_split_amount" DECIMAL(18,2) NOT NULL,
  "refund_split_amount_fen" BIGINT,
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "easy_recon_order_refund_detail" IS '退款事实明细表';
COMMENT ON COLUMN "easy_recon_order_refund_detail"."id" IS '主键 ID';
COMMENT ON COLUMN "easy_recon_order_refund_detail"."order_no" IS '订单号';
COMMENT ON COLUMN "easy_recon_order_refund_detail"."merchant_id" IS '商户 ID';
COMMENT ON COLUMN "easy_recon_order_refund_detail"."refund_split_amount" IS '退款分账金额';
COMMENT ON COLUMN "easy_recon_order_refund_detail"."refund_split_amount_fen" IS '退款分账金额（分）';
COMMENT ON COLUMN "easy_recon_order_refund_detail"."create_time" IS '创建时间';
COMMENT ON COLUMN "easy_recon_order_refund_detail"."update_time" IS '更新时间';

CREATE INDEX IF NOT EXISTS "idx_refund_order_mch" ON "easy_recon_order_refund_detail" ("order_no", "merchant_id");

CREATE TRIGGER update_easy_recon_order_refund_detail_modtime
    BEFORE UPDATE ON "easy_recon_order_refund_detail"
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- 6. 商户维度结算统计表 (结算层)
CREATE TABLE IF NOT EXISTS "easy_recon_order_merchant_settlement" (
  "id" BIGSERIAL PRIMARY KEY,
  "order_no" VARCHAR(64) NOT NULL,
  "merchant_id" VARCHAR(64) NOT NULL,
  "settlement_type" SMALLINT,
  "order_amount_fen" BIGINT DEFAULT 0,
  "split_amount_fen" BIGINT DEFAULT 0,
  "split_fee_fen" BIGINT DEFAULT 0,
  "arrival_amount_fen" BIGINT DEFAULT 0,
  "create_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  "update_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE "easy_recon_order_merchant_settlement" IS '商户维度结算统计表';
COMMENT ON COLUMN "easy_recon_order_merchant_settlement"."id" IS '主键 ID';
COMMENT ON COLUMN "easy_recon_order_merchant_settlement"."order_no" IS '主订单号';
COMMENT ON COLUMN "easy_recon_order_merchant_settlement"."merchant_id" IS '商户 ID';
COMMENT ON COLUMN "easy_recon_order_merchant_settlement"."settlement_type" IS '推算的到账方式 (1:平台代收, 2:全额到商户, 3:空中分账)';
COMMENT ON COLUMN "easy_recon_order_merchant_settlement"."order_amount_fen" IS '订单意图金额 (分)';
COMMENT ON COLUMN "easy_recon_order_merchant_settlement"."split_amount_fen" IS '分账事实金额 (分)';
COMMENT ON COLUMN "easy_recon_order_merchant_settlement"."split_fee_fen" IS '分账手续费 (分)';
COMMENT ON COLUMN "easy_recon_order_merchant_settlement"."arrival_amount_fen" IS '实际到账金额 (分)';
COMMENT ON COLUMN "easy_recon_order_merchant_settlement"."create_time" IS '创建时间';
COMMENT ON COLUMN "easy_recon_order_merchant_settlement"."update_time" IS '更新时间';

CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_merchant" ON "easy_recon_order_merchant_settlement" ("order_no", "merchant_id");
CREATE INDEX IF NOT EXISTS "idx_mch_settle_order_no" ON "easy_recon_order_merchant_settlement" ("order_no");

CREATE TRIGGER update_easy_recon_order_merchant_settlement_modtime
    BEFORE UPDATE ON "easy_recon_order_merchant_settlement"
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- 7. 对账异常记录
CREATE TABLE IF NOT EXISTS "easy_recon_exception" (
  "id" BIGSERIAL PRIMARY KEY,
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

CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_no_exception" ON "easy_recon_exception" ("order_no");

CREATE TRIGGER update_easy_recon_exception_modtime
    BEFORE UPDATE ON "easy_recon_exception"
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- 8. 对账通知日志
CREATE TABLE IF NOT EXISTS "easy_recon_notify_log" (
  "id" BIGSERIAL PRIMARY KEY,
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

CREATE UNIQUE INDEX IF NOT EXISTS "uk_order_no_mch_sub_notify" ON "easy_recon_notify_log" ("order_no", "merchant_id", "sub_order_no");

CREATE TRIGGER update_easy_recon_notify_log_modtime
    BEFORE UPDATE ON "easy_recon_notify_log"
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- 9. 对账规则表
CREATE TABLE IF NOT EXISTS "easy_recon_rule" (
  "id" BIGSERIAL PRIMARY KEY,
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

CREATE UNIQUE INDEX IF NOT EXISTS "uk_rule_name" ON "easy_recon_rule" ("rule_name");

CREATE TRIGGER update_easy_recon_rule_modtime
    BEFORE UPDATE ON "easy_recon_rule"
    FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- 插入默认对账规则
INSERT INTO "easy_recon_rule" ("rule_name", "rule_type", "rule_expression", "rule_desc", "status", "create_time", "update_time")
VALUES 
('默认金额规则', 1, 'payAmount = splitTotalAmount + platformIncome + payFee', '默认金额等式校验规则', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('默认状态规则', 2, 'payStatus && notifyStatus', '默认状态校验规则', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT ("rule_name") DO UPDATE SET update_time = EXCLUDED.update_time;
