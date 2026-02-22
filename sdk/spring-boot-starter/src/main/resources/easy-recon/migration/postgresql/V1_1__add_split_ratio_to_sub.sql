-- Add split_ratio to easy_recon_order_sub
ALTER TABLE "easy_recon_order_sub" ADD COLUMN IF NOT EXISTS "split_ratio" INTEGER;
COMMENT ON COLUMN "easy_recon_order_sub"."split_ratio" IS '分账比例 (基点)';
